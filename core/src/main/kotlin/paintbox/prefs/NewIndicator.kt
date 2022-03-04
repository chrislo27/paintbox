package paintbox.prefs

import paintbox.binding.BooleanVar
import paintbox.util.Version

class NewIndicator(val key: String, val newAsOf: Version, val newEvenIfFirstPlay: Boolean) {
    val value: BooleanVar = BooleanVar(true)
}