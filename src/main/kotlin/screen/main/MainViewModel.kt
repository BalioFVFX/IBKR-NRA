package screen.main

import nav.Navigation
import nav.Screen

class MainViewModel(
    private val navigation: Navigation,
) {

    fun onOpenPositionsClick() {
        navigation.navigateTo(Screen.OpenPositions)
    }

    fun onDividendsClick() {
        navigation.navigateTo(Screen.Dividends)
    }
}