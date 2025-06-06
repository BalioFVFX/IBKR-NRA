package screen.openpositions

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.erikbaliov.ibkr_nra.generated.resources.Res
import com.erikbaliov.ibkr_nra.generated.resources.ic_back
import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import converter.NapOpenPositionConverter
import currency.LevCacheManager
import currency.LevExchanger
import export.NapOpenPositionsExporter
import export.OpenPositionsExporter
import export.TradesExporter
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nav.Navigation
import org.jetbrains.compose.resources.painterResource
import parser.OpenPositionsParser
import parser.TradesParser
import screen.component.FileCell
import screen.util.FileItem
import util.CountryExtractor
import util.DateTimeProvider
import util.FileProvider
import util.WorkBookProvider
import java.io.File
import java.net.http.HttpClient


@Composable
fun OpenPositionsScreen(
    navigation: Navigation,
) {
    val scope = rememberCoroutineScope()
    val viewModel = rememberViewModel(
        scope = scope,
        navigation = navigation,
    )

    val uiState by viewModel.uiState.collectAsState()

    OpenPositionsScreenContent(
        uiState = uiState,
        onTradesImport = viewModel::onTradesImport,
        onOpenPositionsImport = viewModel::onOpenPositionsImport,
        onTradeRemove = viewModel::onRemoveTrade,
        onOpenPositionRemove = viewModel::onRemoveOpenPositions,
        onConvert = viewModel::onConvert,
        onBack = viewModel::onBack,
    )
}

@Composable
fun OpenPositionsScreenContent(
    uiState: OpenPositionsUi,
    onTradesImport: (file: File) -> Unit,
    onOpenPositionsImport: (file: File) -> Unit,
    onConvert: () -> Unit,
    onTradeRemove: (FileItem) -> Unit,
    onOpenPositionRemove: () -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .padding(12.dp)
                .size(32.dp)
                .clickable {
                    onBack.invoke()
                },
            painter = painterResource(Res.drawable.ic_back),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.size(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item(key = "Open positions") {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Файл с отворени позиции",
                    textAlign = TextAlign.Center,
                )
            }

            if (uiState.openPositions != null) {
                item(uiState.openPositions.fileName) {
                    FileCell(
                        name = uiState.openPositions.fileName,
                        isRemoveEnabled = uiState.canRemove,
                        onRemoveClick = { onOpenPositionRemove.invoke() },
                    )
                }
            }

            item(key = "Trades") {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Файлове със сделки",
                    textAlign = TextAlign.Center
                )
            }

            items(uiState.trades) { fileItem ->
                FileCell(
                    name = fileItem.fileName,
                    isRemoveEnabled = uiState.canRemove,
                    onRemoveClick = {
                        onTradeRemove.invoke(fileItem)
                    }
                )
            }
        }

        Row {
            Button(
                content = {
                    Text("Добави файл с отворени позиции")
                },
                onClick = {
                    scope.launch {
                        val platformFile = FileKit.openFilePicker(
                            type = FileKitType.File("csv"),
                        )

                        if (platformFile == null) {
                            return@launch
                        }

                        println("Picked: ${platformFile.name}")

                        onOpenPositionsImport.invoke(platformFile.file)
                    }
                },
                enabled = uiState.canImport
            )

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                content = {
                    Text("Добави файл със сделки")
                },
                onClick = {
                    scope.launch {
                        val platformFile = FileKit.openFilePicker(
                            type = FileKitType.File("csv"),
                        )

                        if (platformFile == null) {
                            return@launch
                        }

                        println("Picked: ${platformFile.name}")

                        onTradesImport.invoke(platformFile.file)
                    }
                },
                enabled = uiState.canImport
            )

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                content = {
                    Text("Конвентирай")
                },
                onClick = {
                    onConvert.invoke()
                },
                enabled = uiState.canConvert
            )
        }
    }
}

@Composable
private fun rememberViewModel(
    scope: CoroutineScope,
    navigation: Navigation,
): OpenPositionsViewModel {
    return remember {
        val workBookProvider = WorkBookProvider()
        val httpClient = HttpClient.newHttpClient()
        val dateTimeProvider = DateTimeProvider()
        val fileProvider = FileProvider(
            dateTimeProvider = dateTimeProvider,
        )

        OpenPositionsViewModel(
            scope = scope,
            openPositionsParser = OpenPositionsParser(reader = csvReader()),
            tradesParser = TradesParser(csvReader()),
            tradesExporter = TradesExporter(workBookProvider = workBookProvider),
            openPositionsExporter = OpenPositionsExporter(workBookProvider = workBookProvider),
            napOpenPositionsExporter = NapOpenPositionsExporter(workBookProvider = workBookProvider),
            dateTimeProvider = dateTimeProvider,
            fileProvider = fileProvider,
            napOpenPositionConverter = NapOpenPositionConverter(
                levExchanger = LevExchanger(
                    httpClient = httpClient,
                    levCacheManager = LevCacheManager(
                        csvReader = CsvReader(),
                        csvWriter = CsvWriter(),
                        fileProvider = fileProvider,
                    )
                ),
                countryExtractor = CountryExtractor(
                    csvReader = csvReader(),
                    fileProvider = fileProvider,
                )
            ),
            navigation = navigation,
        )
    }
}
