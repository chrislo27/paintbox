package paintbox.i18n

import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.ObjectMap
import paintbox.Paintbox
import paintbox.logging.Logger
import java.lang.reflect.InaccessibleObjectException
import java.util.*


data class NamedLocaleBundle(val namedLocale: NamedLocale, val bundle: I18NBundle, val bundleName: String) {

    val internalProperties: Map<String, String> by lazy {
        try {
            val field = bundle::class.java.getDeclaredField("properties")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val map = field.get(bundle) as ObjectMap<String, String>
            map.associate { it.key to it.value }
        } catch (e: InaccessibleObjectException) {
            e.printStackTrace()
            emptyMap()
        }
    }

    val allKeys: Set<String> by lazy {
        val map = internalProperties
        map.keys.toSet()
    }

    /**
     * Keys with missing information.
     */
    val missingKeys: Set<String> = mutableSetOf()

    /**
     * Keys with [IllegalArgumentException]s due to bad formatting.
     * Future IAEs are suppressed.
     */
    val caughtIAEs: Set<String> = mutableSetOf()

    init {
        val actualLocale = bundle.locale
        val requestedLocale = namedLocale.locale
        if (actualLocale != requestedLocale) {
            Paintbox.LOGGER.log(
                if (actualLocale.language != requestedLocale.language) Logger.LogLevel.ERROR else Logger.LogLevel.WARN,
                "NamedLocaleBundle \"$bundleName\" (${namedLocale}) isn't using the same locale as requested. Requested: '$requestedLocale', actual: '$actualLocale'"
            )
        }
    }

    fun getValue(key: String): String {
        if (isKeyMissing(key)) return key
        return bundle[key]
    }

    fun getValue(key: String, vararg args: Any?): String {
        if (isKeyMissing(key)) return key
        return try {
            bundle.format(key, *args)
        } catch (iae: IllegalArgumentException) {
            if (key !in caughtIAEs) {
                (caughtIAEs as MutableSet) += key
                Paintbox.LOGGER.error("IllegalArgumentException thrown when calling getValue on key $key (args [${args.toList()}]). Future IAEs will be suppressed.")
                iae.printStackTrace()
            }
            key
        }
    }

    fun isKeyMissing(key: String): Boolean {
        if (key in missingKeys) return true
        try {
            bundle[key]
        } catch (e: MissingResourceException) {
            (missingKeys as MutableSet) += key
            Paintbox.LOGGER.warn("Missing content for I18N key $key in bundle $bundleName $namedLocale")
            return true
        }
        return false
    }

}