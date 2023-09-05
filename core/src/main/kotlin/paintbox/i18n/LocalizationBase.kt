package paintbox.i18n

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.I18NBundle
import paintbox.Paintbox
import paintbox.binding.BooleanVar
import paintbox.binding.ReadOnlyBooleanVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var


/**
 * Base class for a localization helper. Recommended to create an `object` extension of this class.
 */
abstract class LocalizationBase(
    val baseHandle: FileHandle,
    override val localePicker: LocalePickerBase,
) : ILocalizationWithBundle {

    companion object {

        val DEFAULT_BASE_HANDLE: FileHandle by lazy {
            Gdx.files.internal("localization/default")
        }
    }

    override val bundles: ReadOnlyVar<List<NamedLocaleBundle>> = Var(listOf())
    override val bundlesMap: ReadOnlyVar<Map<NamedLocale, NamedLocaleBundle>> = Var.bind {
        bundles.use().associateBy { it.namedLocale }
    }
    override val currentBundle: ReadOnlyVar<NamedLocaleBundle?> = Var.eagerBind {
        bundlesMap.use()[localePicker.currentLocale.use()]
    }

    init {
        loadBundles()
    }

    protected fun loadBundles() {
        val list = loadBundlesFromLocalePicker(baseHandle)
        (bundles as Var).set(list)
    }

    override fun reloadAll() {
        loadBundles()
    }

    override fun getValue(key: String): String {
        val bundle = currentBundle.getOrCompute() ?: return key
        return bundle.getValue(key)
    }

    override fun getValue(key: String, vararg args: Any?): String {
        val bundle = currentBundle.getOrCompute() ?: return key
        return bundle.getValue(key, *args)
    }

    override fun getVar(key: String): ReadOnlyVar<String> {
        return Var {
            currentBundle.use()?.getValue(key) ?: key
        }
    }

    override fun getVar(key: String, argsProvider: ReadOnlyVar<List<Any?>>): ReadOnlyVar<String> {
        return Var {
            currentBundle.use()?.getValue(key, *argsProvider.use().toTypedArray()) ?: key
        }
    }

    override fun getVar(key: String, staticArgs: List<Any?>): ReadOnlyVar<String> {
        return Var {
            currentBundle.use()?.getValue(key, *staticArgs.toTypedArray()) ?: key
        }
    }

    override fun isKeyMissingInCurrentBundle(key: String): Boolean = currentBundle.getOrCompute()?.isKeyMissing(key) ?: true

    override fun getKeyMissingInCurrentBundleVar(key: String): ReadOnlyBooleanVar = BooleanVar {
        currentBundle.use()?.isKeyMissing(key) ?: true
    }

    protected fun createNamedLocaleBundle(locale: NamedLocale, baseHandle: FileHandle): NamedLocaleBundle {
        return NamedLocaleBundle(
            locale,
            I18NBundle.createBundle(baseHandle, locale.locale, "UTF-8"),
            baseHandle.pathWithoutExtension()
        )
    }

    protected fun loadBundlesFromLocalePicker(basePropertiesHandle: FileHandle): List<NamedLocaleBundle> {
        return localePicker.namedLocales.map {
            createNamedLocaleBundle(it, basePropertiesHandle)
        }
    }

    override fun logMissingLocalizations(showAllKeys: Boolean) {
        val bundles = bundles.getOrCompute()
        val keys: List<String> = bundles.firstOrNull()?.allKeys?.toList() ?: return
        val missing: List<Pair<NamedLocaleBundle, List<String>>> = bundles.drop(1).map { bundle ->
            val props = bundle.internalProperties
            bundle to keys.filter { key -> props.getOrDefault(key, "").isBlank() }.sorted()
        }

        missing.filter { it.second.isNotEmpty() }.forEach {
            Paintbox.LOGGER.warn(
                "Missing ${it.second.size} keys for bundle \"${it.first.bundleName}\" for language ${it.first.namedLocale}${
                    if (showAllKeys) ":${
                        it.second.joinToString(
                            separator = ""
                        ) { i -> "\n  * $i" }
                    }" else ""
                }"
            )
        }
    }

}