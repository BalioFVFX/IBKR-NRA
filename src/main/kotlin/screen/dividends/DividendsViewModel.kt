package screen.dividends

import converter.NapDividendConverter
import export.ConverterErrorExporter
import export.DividendsExporter
import export.NapDividendExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nav.Navigation
import parser.DividendsParser
import screen.util.FileItem
import util.FileProvider
import java.io.File

class DividendsViewModel(
    private val navigation: Navigation,
    private val scope: CoroutineScope,
    private val dividendsParser: DividendsParser,
    private val dividendsExporter: DividendsExporter,
    private val fileProvider: FileProvider,
    private val errorExporter: ConverterErrorExporter,
    private val napDividendExporter: NapDividendExporter,
    private val napDividendConverter: NapDividendConverter,
) {
    private val _uiState = MutableStateFlow(
        DividendsUi(
            dividendFile = null,
            canRemove = false,
            canImport = true,
            canConvert = false,
        )
    )

    val uiState = _uiState.asStateFlow()
    private var internalDividendsFile: File? = null

    fun onImport(file: File) {
        if (!uiState.value.canImport) {
            return
        }

        _uiState.update {
            it.copy(
                dividendFile = FileItem(
                    id = 1,
                    fileName = file.name,
                ),
                canRemove = true,
                canConvert = true,
            )
        }

        internalDividendsFile = file
    }

    fun onDividendFileRemove() {
        if (!uiState.value.canRemove) {
            return
        }

        _uiState.update {
            it.copy(
                dividendFile = null,
                canConvert = false,
            )
        }

        internalDividendsFile = null
    }

    fun onConvert() {
        if (!uiState.value.canConvert) {
            return
        }

        val previousState = _uiState.value
        _uiState.update {
            it.copy(
                canImport = false,
                canRemove = false,
                canConvert = false,
            )
        }

        scope.launch(Dispatchers.IO) {
            val dividendsResult = dividendsParser.parse(internalDividendsFile!!)

            if (dividendsResult.isFailure) {
                dividendsResult.exceptionOrNull()?.printStackTrace()
                _uiState.value = previousState
                return@launch
            }

            val dividends = dividendsResult.getOrNull()!!

            val exportDir = fileProvider.provideExportDirectory()

            val debugExportResult = dividendsExporter.export(
                dividends = dividends,
                sheetName = "Дивиденти",
                destination = fileProvider.provide(
                    parentFilePath = exportDir.debug,
                    childPath = "/dividends.xls"
                ),
            )

            if (debugExportResult.isFailure) {
                _uiState.value = previousState
                debugExportResult.exceptionOrNull()?.printStackTrace()
                return@launch
            }

            val napConverterResult = napDividendConverter.convert(dividends = dividends)

            if (napConverterResult.errors.isNotEmpty()) {
                errorExporter.export(
                    destination = fileProvider.provide(
                        parentFilePath = exportDir.export,
                        childPath = "errors.txt",
                    ),
                    errors = napConverterResult.errors,
                )
            }

            val exportResult = napDividendExporter.export(
                dividends = napConverterResult.data,
                destination = fileProvider.provide(
                    parentFilePath = exportDir.export,
                    childPath = "result.xls",
                )
            )

            if (exportResult.isFailure) {
                println("Could not export Nap Dividends")
            }


            _uiState.value = previousState
        }
    }

    fun onBack() {
        if (!uiState.value.canImport) {
            return
        }

        navigation.onBack()
    }
}