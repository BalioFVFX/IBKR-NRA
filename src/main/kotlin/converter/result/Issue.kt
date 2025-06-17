package converter.result

import converter.result.Issue.Error
import converter.result.Issue.Warning

sealed class Issue(val reason: String) {
    class Error(reason: String) : Issue(reason)
    class Warning(reason: String) : Issue(reason)
}

fun MutableList<Issue>.addError(reason: String) {
    println(reason)
    add(Error(reason = reason))
}

fun MutableList<Issue>.addWarning(reason: String) {
    println(reason)
    add(Warning(reason = reason))
}