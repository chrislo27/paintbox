package paintbox.i18n

import paintbox.binding.ReadOnlyVar


interface ILocalizationWithBundle : ILocalization {

    val bundles: ReadOnlyVar<List<NamedLocaleBundle>>
    val bundlesMap: ReadOnlyVar<Map<NamedLocale, NamedLocaleBundle>>
    val currentBundle: ReadOnlyVar<NamedLocaleBundle?>

}