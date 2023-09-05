package paintbox.i18n

import paintbox.binding.ReadOnlyBooleanVar
import paintbox.binding.ReadOnlyVar


interface ILocalization {

    val localePicker: LocalePickerBase

    //region Getters

    /**
     * Returns the current value for the given key.
     */
    fun getValue(key: String): String

    /**
     * Returns the current value for the given key, with substitution arguments provided.
     */
    fun getValue(key: String, vararg args: Any?): String

    /**
     * Returns a [ReadOnlyVar] representing the value for the given key.
     */
    fun getVar(key: String): ReadOnlyVar<String>

    /**
     * Returns a [ReadOnlyVar] representing the value for the given key, with [argsProvider] being a [ReadOnlyVar]
     * returning the list of arguments.
     */
    fun getVar(key: String, argsProvider: ReadOnlyVar<List<Any?>>): ReadOnlyVar<String>

    /**
     * Returns a [ReadOnlyVar] representing the value for the given key with [staticArgs] being static arguments.
     */
    fun getVar(key: String, staticArgs: List<Any?>): ReadOnlyVar<String>

    //endregion


    //region Loading

    /**
     * Reloads localizations from disk.
     */
    fun reloadAll()

    /**
     * Returns true if the given [key] is missing from the current I18N bundle.
     * If the current bundle is null, returns true for any input (no bundle = no keys).
     */
    fun isKeyMissingInCurrentBundle(key: String): Boolean

    /**
     * @see isKeyMissingInCurrentBundle
     */
    fun getKeyMissingInCurrentBundleVar(key: String): ReadOnlyBooleanVar

    /**
     * Logs missing localization keys.
     */
    fun logMissingLocalizations(showAllKeys: Boolean = true)

    //endregion

}
