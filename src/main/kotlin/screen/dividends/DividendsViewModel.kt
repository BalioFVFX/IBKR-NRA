package screen.dividends

import converter.NapDividendConverter
import converter.result.Issue
import export.ConverterIssueExporter
import export.DividendsExporter
import export.NapDividendExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nav.Navigation
import parser.DividendsParser
import screen.dialog.ConversionCompleteDialogUi
import screen.dialog.ErrorDialogUi
import screen.util.FileItem
import util.DirectoryOpener
import util.ExportDirectory
import util.FileProvider
import util.combineProgress
import java.io.File

class DividendsViewModel(
    private val navigation: Navigation,
    private val scope: CoroutineScope,
    private val dividendsParser: DividendsParser,
    private val dividendsExporter: DividendsExporter,
    private val fileProvider: FileProvider,
    private val errorExporter: ConverterIssueExporter,
    private val napDividendExporter: NapDividendExporter,
    private val napDividendConverter: NapDividendConverter,
    private val directoryOpener: DirectoryOpener,
) {
    private val _uiState = MutableStateFlow(
        DividendsUi(
            dividendFile = null,
            canRemove = false,
            canImport = true,
            canConvert = false,
            progress = null,
            conversionCompleteDialogUi = null,
            errorDialogUi = null,
        )
    )

    val uiState = _uiState.asStateFlow()

    private var internalDividendsFile: File? = null
    @Volatile private var lastExportDir: ExportDirectory? = null

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

        val progressJob = scope.launch {
            combineProgress(
                dividendsParser.progress,
                napDividendConverter.progress,
                dividendsExporter.progress,
                napDividendExporter.progress,
            ).collect { progress ->
                _uiState.update { it.copy(progress = progress.value) }
            }
        }

        progressJob.invokeOnCompletion {
            println("Cancelling progress collection")
            _uiState.update { it.copy(progress = null) }
        }

        scope.launch(Dispatchers.IO) {
            val dividendsResult = dividendsParser.parse(internalDividendsFile!!)

            if (dividendsResult.isFailure) {
                dividendsResult.exceptionOrNull()?.printStackTrace()
                _uiState.update {
                    previousState.copy(
                        errorDialogUi = ErrorDialogUi(message = dividendsResult.exceptionOrNull()?.message ?: "")
                    )
                }
                return@launch
            }

            val dividends = dividendsResult.getOrNull()!!

            val exportDir = fileProvider.provideExportDirectory()
            lastExportDir = exportDir

            val debugExportResult = dividendsExporter.export(
                dividends = dividends,
                sheetName = "Дивиденти",
                destination = fileProvider.provide(
                    parentFilePath = exportDir.debug,
                    childPath = "/dividends.xls"
                ),
            )

            if (debugExportResult.isFailure) {
                debugExportResult.exceptionOrNull()?.printStackTrace()
                _uiState.update {
                    previousState.copy(
                        errorDialogUi = ErrorDialogUi(message = "Неуспешно записване на debug/dividends.xls файл")
                    )
                }
                return@launch
            }

            val napConverterResult = napDividendConverter.convert(dividends = dividends)

            if (napConverterResult.issues.isNotEmpty()) {
                errorExporter.export(
                    destination = fileProvider.provide(
                        parentFilePath = exportDir.export,
                        childPath = "errors.txt",
                    ),
                    issues = napConverterResult.issues,
                )
            }

            val exportResult = napDividendExporter.export(
                dividends = napConverterResult.data,
                destination = fileProvider.provide(
                    parentFilePath = exportDir.export,
                    childPath = "result.xls",
                )
            )

            if (!exportResult.isSuccess)  {
                _uiState.update {
                    previousState.copy(
                        errorDialogUi = ErrorDialogUi(message = "Неуспешно записване на result.xls файл")
                    )
                }

                return@launch
            }

            _uiState.update {
                previousState.copy(
                    conversionCompleteDialogUi = ConversionCompleteDialogUi(
                        notices = napConverterResult.issues.count { it is Issue.Warning },
                        errors = napConverterResult.issues.count { it is Issue.Error },
                    )
                )
            }
        }.invokeOnCompletion {
            progressJob.cancel()
        }
    }

    fun onBack() {
        if (!uiState.value.canImport) {
            return
        }

        navigation.onBack()
    }

    fun onCloseErrorDialog() {
        _uiState.update {
            it.copy(
                errorDialogUi = null,
            )
        }
    }

    fun onCloseConversionDialog(openResultsDirectory: Boolean) {
        _uiState.update {
            it.copy(
                conversionCompleteDialogUi = null,
            )
        }

        if (!openResultsDirectory) {
            return
        }

        lastExportDir?.let {
            directoryOpener.openDirectory(path = it.export.absolutePath)
        }
    }
}