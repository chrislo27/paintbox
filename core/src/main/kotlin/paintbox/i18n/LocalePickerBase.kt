package paintbox.i18n

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Json
import paintbox.binding.Var
import java.util.*


/**
 * Holds a [NamedLocale] and can attempt to find the closest one given a setting.
 * 
 * Can be used across multiple [LocalizationBase]s to synchronize their bundle state. 
 */
open class LocalePickerBase(val langDefFile: FileHandle) {
    
    companion object {
        val DEFAULT_LANG_DEFINITION_FILE: FileHandle by lazy {
            Gdx.files.internal("localization/langs.json")
        }
    }

    val namedLocales: List<NamedLocale> = getBundlesFromLangFile().takeUnless { it.isEmpty() } ?: error("Loaded locales must be non-empty. Loaded from $langDefFile")
    val currentLocale: Var<NamedLocale> = Var(namedLocales.first())
    
    
    protected fun getBundlesFromLangFile(): List<NamedLocale> {
        return Json().fromJson(Array<LanguageObject>::class.java, langDefFile)
            .map(LanguageObject::toNamedLocale)
    }

    /**
     * Attempts to find the best [NamedLocale] by the locale string (`lang_country_variant`).
     * If the exact variant cannot be found, defaults to just `lang_country`. And if that couldn't be found,
     * defaults to just `lang`.
     * @return The closest matching [NamedLocale], where the language is at least the same, or null if the locale couldn't be found at all.
     */
    fun attemptToFindNamedLocale(localeStr: String): NamedLocale? {
        val locales = namedLocales
        if (localeStr == "") {
            return locales.find { it.locale == Locale.ROOT }
        }
        
        val split = localeStr.split('_')
        val language = split.first()
        val country = split.getOrNull(1)
        val variant = split.getOrNull(2)

        val correctLocaleBundle = locales.find {
            it.locale.language == language && it.locale.country == country && it.locale.variant == variant
        } ?: locales.find {
            it.locale.language == language && it.locale.country == country
        } ?: locales.find {
            it.locale.language == language
        }
        
        return correctLocaleBundle
    }
    
    fun attemptToSetNamedLocale(localeStr: String): NamedLocale {
        val picked = attemptToFindNamedLocale(localeStr) ?: namedLocales.first()
        currentLocale.set(picked)
        return picked
    }
    
}
