package paintbox.i18n

import paintbox.binding.ReadOnlyVar


interface ILocalizationWithBundle : ILocalization {

    val bundles: ReadOnlyVar<List<NamedLocaleBundle>>
    val bundlesMap: ReadOnlyVar<Map<NamedLocale, NamedLocaleBundle>>
    val currentBundle: ReadOnlyVar<NamedLocaleBundle?>
    

    override fun getAllUniqueKeysForAllLocales(): Set<String> {
        val uniqueKeys = mutableSetOf<String>()
        bundles.getOrCompute().forEach {
            uniqueKeys.addAll(it.allKeys)
        }
        return uniqueKeys
    }

}