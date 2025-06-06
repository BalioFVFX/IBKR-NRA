package nav

import kotlinx.coroutines.flow.MutableStateFlow

class Navigation {

    private val backStack = ArrayDeque<Screen>().apply {
        addLast(Screen.Main)
    }
    val currentScreen = MutableStateFlow<Screen>(Screen.Main)

    fun navigateTo(
        screen: Screen,
    ) {
        backStack.addLast(screen)
        currentScreen.value = screen
    }

    fun onBack() {
        if (backStack.size <= 1) {
            return
        }

        backStack.removeLast()

        currentScreen.value = backStack.last()
    }
}