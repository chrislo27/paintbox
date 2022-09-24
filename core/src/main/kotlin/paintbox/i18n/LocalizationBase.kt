package paintbox.i18n

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import com.badlogic.gdx.utils.ObjectMap
import paintbox.Paintbox
import paintbox.binding.BooleanVar
import paintbox.binding.ReadOnlyBooleanVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var


/**
 * Base class for a localization helper. Recommended to create an `object` extension of this class.
 */
abstract class LocalizationBase(val baseHandle: FileHandle, val localePicker: LocalePickerBase) {

    companion object {
        val DEFAULT_BASE_HANDLE: FileHandle by lazy {
            Gdx.files.internal("localization/default")
        }
    }

    val bundles: ReadOnlyVar<List<NamedLocaleBundle>> = Var(listOf())
    val bundlesMap: ReadOnlyVar<Map<NamedLocale, NamedLocaleBundle>> = Var.bind { 
        bundles.use().associateBy { it.namedLocale }
    }
    val currentBundle: ReadOnlyVar<NamedLocaleBundle?> = Var.eagerBind { 
        bundlesMap.use()[localePicker.currentLocale.use()]
    }
    
    init {
        loadBundles()
    }
    
    protected fun loadBundles() {
        val list = loadBundlesFromLocalePicker(baseHandle)
        (bundles as Var).set(list)
    }

    open fun reloadAll() {
        loadBundles()
    }

    /**
     * Returns the current value for the given key.
     */
    fun getValue(key: String): String {
        val bundle = currentBundle.getOrCompute() ?: return key
        return bundle.getValue(key)
    }

    /**
     * Returns the current value for the given key, with substitution arguments provided.
     */
    fun getValue(key: String, vararg args: Any?): String {
        val bundle = currentBundle.getOrCompute() ?: return key
        return bundle.getValue(key, *args)
    }

    /**
     * Returns a [ReadOnlyVar] representing the value for the given key.
     */
    fun getVar(key: String): ReadOnlyVar<String> {
        return Var {
            currentBundle.use()?.getValue(key) ?: key
        }
    }

    /**
     * Returns a [ReadOnlyVar] representing the value for the given key, with [argsProvider] being a [ReadOnlyVar]
     * returning the list of arguments.
     */
    fun getVar(key: String, argsProvider: ReadOnlyVar<List<Any?>>): ReadOnlyVar<String> {
        return Var {
            currentBundle.use()?.getValue(key, *argsProvider.use().toTypedArray()) ?: key
        }
    }

    /**
     * Returns a [ReadOnlyVar] representing the value for the given key with [staticArgs] being static arguments.
     */
    fun getVar(key: String, staticArgs: List<Any?>): ReadOnlyVar<String> {
        return Var {
            currentBundle.use()?.getValue(key, *staticArgs.toTypedArray()) ?: key
        }
    }

    /**
     * Returns true if the given [key] is missing from [currentBundle].
     * If [currentBundle] is null, returns true (no bundle = no keys).
     */
    fun isKeyMissingInCurrentBundle(key: String): Boolean = currentBundle.getOrCompute()?.isKeyMissing(key) ?: true

    /**
     * @see isKeyMissingInCurrentBundle
     */
    fun getKeyMissingInCurrentBundleVar(key: String): ReadOnlyBooleanVar = BooleanVar {
        currentBundle.use()?.isKeyMissing(key) ?: true
    }

    protected fun createNamedLocaleBundle(locale: NamedLocale, baseHandle: FileHandle): NamedLocaleBundle {
        return NamedLocaleBundle(locale, I18NBundle.createBundle(baseHandle, locale.locale, "UTF-8"), baseHandle.pathWithoutExtension())
    }

    protected fun loadBundlesFromLocalePicker(basePropertiesHandle: FileHandle): List<NamedLocaleBundle> {
        return localePicker.namedLocales.map {
            createNamedLocaleBundle(it, basePropertiesHandle)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun logMissingLocalizations() {
        val bundles = bundles.getOrCompute()
        val keys: List<String> = bundles.firstOrNull()?.allKeys?.toList() ?: return
        val missing: List<Pair<NamedLocaleBundle, List<String>>> = bundles.drop(1).map { tbundle ->
            val bundle = tbundle.bundle
            val field = bundle::class.java.getDeclaredField("properties")
            field.isAccessible = true
            val objMap = field.get(bundle) as ObjectMap<String, String>
            val normalMap = objMap.associate { it.key to it.value }

            tbundle to (keys.filter { key -> normalMap.getOrDefault(key, "").isNotBlank() }).sorted()
        }

        missing.filter { it.second.isNotEmpty() }.forEach {
            Paintbox.LOGGER.warn("Missing ${it.second.size} keys for bundle ${it.first.namedLocale}:${it.second.joinToString(separator = "") { i -> "\n  * $i" }}")
        }
    }

}