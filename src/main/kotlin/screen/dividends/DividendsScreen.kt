package screen.dividends

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import converter.NapDividendConverter
import currency.LevCacheManager
import currency.LevExchanger
import export.ConverterIssueExporter
import export.DividendsExporter
import export.NapDividendExporter
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nav.Navigation
import org.jetbrains.compose.resources.painterResource
import parser.DividendsParser
import screen.component.FileCell
import screen.component.ProgressIndicator
import screen.util.FileItem
import util.CompanyNameExtractor
import util.CountryExtractor
import util.DateTimeProvider
import util.FileProvider
import util.PercentageCalculator
import util.WorkBookProvider
import java.io.File
import java.net.http.HttpClient

@Composable
fun DividendsScreen(navigation: Navigation) {
    val scope = rememberCoroutineScope()

    val viewModel = rememberViewModel(
        scope = scope,
        navigation = navigation,
    )

    val uiState by viewModel.uiState.collectAsState()

    DividendsScreenContent(
        dividendsUi = uiState,
        onBack = viewModel::onBack,
        onDividendFileRemove = viewModel::onDividendFileRemove,
        onConvert = viewModel::onConvert,
        onImport = viewModel::onImport,
    )


}

@Composable
fun DividendsScreenContent(
    dividendsUi: DividendsUi,
    onBack: () -> Unit,
    onDividendFileRemove: () -> Unit,
    onConvert: () -> Unit,
    onImport: (File) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(modifier = Modifier.weight(1f)) {
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

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Файл с дивиденти",
                textAlign = TextAlign.Center,
            )

            dividendsUi.dividendFile?.let { fileItem ->
                FileCell(
                    name = fileItem.fileName,
                    isRemoveEnabled = dividendsUi.canRemove,
                    onRemoveClick = onDividendFileRemove,
                )
            }
        }

        Row {
            Button(
                enabled = dividendsUi.canImport,
                onClick = {
                    scope.launch {
                        val platformFile = FileKit.openFilePicker(
                            type = FileKitType.File("csv"),
                        )

                        if (platformFile == null) {
                            return@launch
                        }

                        println("Picked: ${platformFile.name}")

                        onImport.invoke(platformFile.file)
                    }
                },
                content = {
                    Text("Добави файл")
                }
            )

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                enabled = dividendsUi.canConvert,
                onClick = onConvert,
                content = {
                    Text("Конвентирай")
                }
            )
        }

        if (dividendsUi.progress != null) {
            Spacer(Modifier.size(8.dp))
            ProgressIndicator(percent = dividendsUi.progress)
        }
    }
}

@Preview
@Composable
private fun DividendsScreenPreview() {
    DividendsScreenContent(
        dividendsUi = DividendsUi(
            dividendFile = FileItem(
                id = 1,
                fileName = "Dividends-2024.csv"
            ),
            canRemove = true,
            canImport = true,
            canConvert = true,
            progress = null,
        ),
        onBack = {},
        onDividendFileRemove = {},
        onConvert = {},
        onImport = {},
    )
}

@Composable
private fun rememberViewModel(
    scope: CoroutineScope,
    navigation: Navigation
): DividendsViewModel {
    return remember {
        val dateTimeProvider = DateTimeProvider()
        val fileProvider = FileProvider(dateTimeProvider)
        val workBookProvider = WorkBookProvider()
        val httpClient = HttpClient.newHttpClient()

        val levExchanger = LevExchanger(
            httpClient = httpClient,
            levCacheManager = LevCacheManager(
                csvReader = csvReader(),
                csvWriter = csvWriter(),
                fileProvider = fileProvider,
            ),
        )

        val companyNameExtractor = CompanyNameExtractor(
            csvReader = csvReader(),
            fileProvider = fileProvider,
        )

        val countryExtractor = CountryExtractor(
            csvReader = csvReader(),
            fileProvider = fileProvider,
        )

        DividendsViewModel(
            navigation = navigation,
            scope = scope,
            dividendsParser = DividendsParser(
                csvReader = csvReader(),
                percentageCalculator = PercentageCalculator(),
            ),
            fileProvider = fileProvider,
            dividendsExporter = DividendsExporter(
                workBookProvider = workBookProvider,
                percentageCalculator = PercentageCalculator(),
            ),
            errorExporter = ConverterIssueExporter(),
            napDividendExporter = NapDividendExporter(
                workBookProvider = workBookProvider,
                percentageCalculator = PercentageCalculator(),
            ),
            napDividendConverter = NapDividendConverter(
                levExchanger = levExchanger,
                companyNameExtractor = companyNameExtractor,
                countryExtractor = countryExtractor,
                percentageCalculator = PercentageCalculator(),
            ),
        )
    }
}