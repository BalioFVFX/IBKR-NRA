import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nav.Navigation
import nav.Screen
import screen.dividends.DividendsScreen
import screen.main.MainScreen
import screen.openpositions.OpenPositionsScreen

@Composable
@Preview
fun App(
    navigation: Navigation,
) {
    MaterialTheme {
        val currentScreen by navigation.currentScreen.collectAsState()
        when (currentScreen) {
            Screen.Main -> {
                MainScreen(navigation)
            }
            Screen.OpenPositions -> {
                OpenPositionsScreen(navigation)
            }
            Screen.Dividends -> {
                DividendsScreen(navigation)
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "IBKR-NRA",
    ) {
        App(Navigation())
    }
}
