package screen.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nav.Navigation

@Composable
fun MainScreen(
    navigation: Navigation,
) {
    val viewModel = remember { MainViewModel(navigation) }

    MainScreenContent(
        onOpenPositionsClick = {
            viewModel.onOpenPositionsClick()
        },
        onDividendsClick = {
            viewModel.onDividendsClick()
        }

    )
}

@Composable
fun MainScreenContent(
    onOpenPositionsClick: () -> Unit,
    onDividendsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            modifier = Modifier
                .align(alignment = Alignment.TopCenter),
            text = "IBKR-NRA",
        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onOpenPositionsClick,
                content = {
                    Text("Отворени позиции (Приложение 8)")
                }
            )

            Spacer(modifier = Modifier.size(8.dp))

            Button(
                onClick = onDividendsClick,
                content = {
                    Text("Дивиденти (Приложение 8)")
                }
            )
        }
    }

}

@Composable
@Preview
private fun MainScreenPreview() {
    MainScreenContent(
        onOpenPositionsClick = {},
        onDividendsClick = {},
    )
}