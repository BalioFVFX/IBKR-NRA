package screen.openpositions

import converter.NapOpenPositionConverter
import export.NapOpenPositionsExporter
import export.OpenPositionsExporter
import export.TradesExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nav.Navigation
import parser.output.OpenPosition
import parser.OpenPositionsParser
import parser.output.Trade
import parser.TradesParser
import screen.util.FileItem
import util.DateTimeProvider
import util.FileProvider
import util.combineProgress
import java.io.File
import java.time.format.DateTimeFormatter

class OpenPositionsViewModel(
    private val scope: CoroutineScope,
    private val openPositionsParser: OpenPositionsParser,
    private val tradesParser: TradesParser,
    private val tradesExporter: TradesExporter,
    private val openPositionsExporter: OpenPositionsExporter,
    private val napOpenPositionsExporter: NapOpenPositionsExporter,
    private val dateTimeProvider: DateTimeProvider,
    private val napOpenPositionConverter: NapOpenPositionConverter,
    private val fileProvider: FileProvider,
    private val navigation: Navigation,
) {

    companion object {
        private val EXPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY_HH.mm.ss")
    }

    private var idGenerator = 0L

    private val internalTrades =  mutableListOf<Pair<FileItem, List<Trade>>>()
    private var internalOpenPositions: Pair<FileItem, List<OpenPosition>>? = null

    private val _uiState = MutableStateFlow(
        OpenPositionsUi(
            trades = emptyList(),
            openPositions = null,
            canImport = true,
            canConvert = false,
            canRemove = true,
            progress = null,
        )
    )

    val uiState = _uiState.asStateFlow()

    fun onTradesImport(file: File) {
        _uiState.update { data ->
            data.copy(
                canImport = false,
            )
        }

        scope.launch(Dispatchers.IO) {
            val result = tradesParser.parse(file)

            if (result.isFailure) {
                result.exceptionOrNull()!!.printStackTrace()

                _uiState.update { data ->
                    data.copy(
                        canImport = true,
                    )
                }

                return@launch
            }

            internalTrades.add(FileItem(id = idGenerator++, fileName = file.name) to result.getOrThrow())

            _uiState.update { data ->
                data.copy(
                    trades = internalTrades.map { it.first },
                    canImport = true,
                    canConvert = computeCanConvert(isConverting = false)
                )
            }
        }
    }

    fun onOpenPositionsImport(file: File) {
        _uiState.value = _uiState.value.copy(
            canImport = false,
        )

        scope.launch(Dispatchers.IO) {
            val result = openPositionsParser.parse(file)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    canImport = true,
                )

                result.exceptionOrNull()!!.printStackTrace()
                return@launch
            }


            internalOpenPositions = FileItem(id = idGenerator++, fileName = file.name) to result.getOrThrow()

            _uiState.value = _uiState.value.copy(
                canImport = true,
                canConvert = computeCanConvert(isConverting = false),
                openPositions = internalOpenPositions!!.first,
            )
        }
    }

    fun onConvert() {
        val previousState = _uiState.value

        _uiState.value = _uiState.value.copy(
            canImport = false,
            canConvert = computeCanConvert(isConverting = true),
            canRemove = false,
        )

        val progressJob = scope.launch {
            combineProgress(
                tradesExporter.progress,
                openPositionsExporter.progress,
                napOpenPositionsExporter.progress,
                napOpenPositionConverter.progress,
            ).collect { progress ->
                _uiState.update { uiState ->
                    uiState.copy(progress = progress.value)
                }
            }
        }

        progressJob.invokeOnCompletion {
            println("Cancelling progress collection")
            _uiState.update { uiState ->
                uiState.copy(progress = null)
            }
        }

        scope.launch(Dispatchers.IO) {
            val basePath = "exports/${dateTimeProvider.currentDateTime().format(EXPORT_DATE_TIME_FORMATTER)}"

            fileProvider.provide("${basePath}/debug/").mkdirs()

            val tradesExportResult = tradesExporter.export(
                trades = internalTrades.flatMap { it.second },
                sheetName = "Всички сделки",
                destination = File("${basePath}/debug/trades.xls"),
            )

            if (!tradesExportResult.isSuccess) {
                _uiState.update { previousState}
                return@launch
            }

            val openPositionsExportResult = openPositionsExporter.export(
                openPositions = internalOpenPositions!!.second,
                sheetName = "Отворени позиции",
                destination = File("${basePath}/debug/open_positions.xls")
            )

            if (!openPositionsExportResult.isSuccess) {
                _uiState.update { previousState }
                return@launch
            }

            val convertResult = napOpenPositionConverter.convert(
                trades = internalTrades.flatMap { it.second },
                openPositions = internalOpenPositions!!.second,
            )

            if (convertResult.errors.isNotEmpty()) {
                fileProvider.provide("${basePath}/errors.txt").writeText(
                    convertResult.errors.joinToString(separator = "\n")
                )
            }

            napOpenPositionsExporter.export(
                openPositions = convertResult.data,
                destination = fileProvider.provide("${basePath}/result.xls"),
            )

            _uiState.update { previousState }
        }.invokeOnCompletion {
            progressJob.cancel()
        }
    }

    fun onRemoveTrade(fileItem: FileItem) {
        internalTrades.indexOfFirst { it.first == fileItem }.takeIf { it != -1 }?.let { index ->
            internalTrades.removeAt(index)
        }

        _uiState.update { data ->
            data.copy(
                trades = internalTrades.map { it.first },
                canConvert = computeCanConvert(isConverting = false)
            )
        }
    }

    fun onRemoveOpenPositions() {
        internalOpenPositions = null

        _uiState.update { data ->
            data.copy(
                openPositions = null,
                canConvert = computeCanConvert(isConverting = false),
            )
        }
    }

    fun onBack() {
        if (!uiState.value.canRemove) {
            return
        }

        navigation.onBack()
    }

    private fun computeCanConvert(isConverting: Boolean) : Boolean {
        return !isConverting && internalOpenPositions != null && internalTrades.isNotEmpty()
    }
}