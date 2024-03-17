package paintbox.i18n

import paintbox.Paintbox
import paintbox.binding.*


/**
 * Represents a collection of [ILocalization] instances grouped as one.
 * 
 * Calling the relevant get functions will fetch it from the appropriate [ILocalization] instance.
 * 
 * All [ILocalization] instances passed in must have the same [localePicker] as this group instance.
 */
open class LocalizationGroup(
    override val localePicker: LocalePickerBase,
    val localizationInstances: List<ILocalization>,
) : ILocalization {

    companion object {

        private const val LOGGING_TAG: String = "LocalizationGroup"
    }

    private val keysToBundles: Var<Map<String, ILocalization>> = Var(mapOf())

    init {
        localizationInstances.forEachIndexed { index, l10n ->
            if (l10n.localePicker != this.localePicker) {
                error("Localization instance[$index] $l10n (${l10n::class.java.name}) does not share the same LocalePickerBase as this LocalizationGroup")
            }
        }
        learnKeyMappings()
    }

    override fun getValue(key: String): String {
        val mapped = getLocalizationInstanceForKey(key)?.getValue(key)
        if (mapped != null) return mapped
        
        // Fallback iteration -- note that this logs a missing key warning for each ILocalization instance
        for (loc in localizationInstances) {
            if (!loc.isKeyMissing(key)) return loc.getValue(key)
        }
        
        return key
    }

    override fun getValue(key: String, vararg args: Any?): String {
        val mapped = getLocalizationInstanceForKey(key)?.getValue(key, *args)
        if (mapped != null) return mapped

        // Fallback iteration -- note that this logs a missing key warning for each ILocalization instance
        for (loc in localizationInstances) {
            if (!loc.isKeyMissing(key)) return loc.getValue(key, *args)
        }
        return key
    }

    override fun getVar(key: String): ReadOnlyVar<String> {
        return Var {
            val mappedVar = useLocalizationInstanceForKey(key)?.getVar(key)
            if (mappedVar != null) return@Var mappedVar.use()

            // Fallback iteration -- note that this logs a missing key warning for each ILocalization instance
            for (loc in localizationInstances) {
                if (!loc.getIsKeyMissingVar(key).use()) return@Var loc.getVar(key).use()
            }
            key
        }
    }

    override fun getVar(key: String, argsProvider: ReadOnlyVar<List<Any?>>): ReadOnlyVar<String> {
        return Var {
            val mappedVar = useLocalizationInstanceForKey(key)?.getVar(key, argsProvider)
            if (mappedVar != null) return@Var mappedVar.use()

            // Fallback iteration -- note that this logs a missing key warning for each ILocalization instance
            for (loc in localizationInstances) {
                if (!loc.getIsKeyMissingVar(key).use()) return@Var loc.getVar(key, argsProvider).use()
            }
            key
        }
    }

    override fun getVar(key: String, staticArgs: List<Any?>): ReadOnlyVar<String> {
        return Var {
            val mappedVar = useLocalizationInstanceForKey(key)?.getVar(key, staticArgs)
            if (mappedVar != null) return@Var mappedVar.use()

            // Fallback iteration -- note that this logs a missing key warning for each ILocalization instance
            for (loc in localizationInstances) {
                if (!loc.getIsKeyMissingVar(key).use()) return@Var loc.getVar(key, staticArgs).use()
            }
            key
        }
    }

    override fun getAllUniqueKeysForAllLocales(): Set<String> {
        return localizationInstances.flatMap { it.getAllUniqueKeysForAllLocales() }.toSet()
    }

    override fun reloadAll() {
        localizationInstances.forEach { it.reloadAll() }
        learnKeyMappings()
    }

    override fun isKeyMissing(key: String): Boolean {
        for (loc in localizationInstances) {
            if (!loc.isKeyMissing(key)) return false
        }
        return true
    }

    override fun getIsKeyMissingVar(key: String): ReadOnlyBooleanVar {
        return BooleanVar {
            for (loc in localizationInstances) {
                if (!loc.getIsKeyMissingVar(key).use()) return@BooleanVar false
            }
            true
        }
    }

    override fun logMissingLocalizations(showAllKeys: Boolean) {
        localizationInstances.forEach { it.logMissingLocalizations(showAllKeys) }
    }

    private fun learnKeyMappings() {
        val map = mutableMapOf<String, ILocalization>()
        localizationInstances.forEach { loc ->
            loc.getAllUniqueKeysForAllLocales().forEach { key ->
                if (key !in map) {
                    map[key] = loc
                } else {
                    Paintbox.LOGGER.warn("Key \"${key}\" in $loc was already mapped to ${map[key]} when learning key mappings", tag = LOGGING_TAG)
                }
            }
        }
        this.keysToBundles.set(map)
    }
    
    private fun getLocalizationInstanceForKey(key: String): ILocalization? =
        this.keysToBundles.getOrCompute()[key]
    
    private fun VarContext.useLocalizationInstanceForKey(key: String): ILocalization? =
        this.use(keysToBundles)[key]
}