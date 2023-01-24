package paintbox.i18n

import java.util.*


class LanguageObject {

    lateinit var name: String
    lateinit var locale: LocaleObject

    fun toNamedLocale(): NamedLocale =
        NamedLocale(name, Locale(locale.language ?: "", locale.country ?: "", locale.variant ?: ""))

}

/**
 * See [com.badlogic.gdx.utils.I18NBundle] for valid locale formats.
 *
 * The fields should be all present in order of [language], then [country], then [variant]. Having all fields be NOT
 * present is also legal.
 * Variants may be created for just a country (with no language) or just a language (with no country), but
 * not when language and country are not present.
 */
class LocaleObject {

    var language: String? = null
    var country: String? = null
    var variant: String? = null

}
