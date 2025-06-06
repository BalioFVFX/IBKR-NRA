package nav

sealed class Screen {
    object Main : Screen()
    object OpenPositions: Screen()
    object Dividends : Screen()
}