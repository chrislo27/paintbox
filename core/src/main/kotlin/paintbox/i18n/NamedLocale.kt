package paintbox.i18n

import java.util.*


data class NamedLocale(val name: String, val locale: Locale, val metadata: Map<String, String> = emptyMap()) {

    override fun toString(): String {
        return "$name ($locale)"
    }
}