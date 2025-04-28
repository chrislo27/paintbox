package paintbox.binding

/**
 * Represents an immutable var. For mutable vars, see [Var].
 *
 * The default mutable implementation is [GenericVar]. Note that [GenericVar] is mutable since it also implements the
 * [Var] interface; this is similar to the Kotlin [List] and [MutableList] default implementations that
 * use [ArrayList] which is mutable.
 *
 * The default immutable implementation can be instantiated using the [Companion.const] function. There are also
 * similar functions for each of the primitive specializations ([ReadOnlyBooleanVar], [ReadOnlyDoubleVar],
 * [ReadOnlyFloatVar], [ReadOnlyIntVar], [ReadOnlyLongVar], and [ReadOnlyCharVar]).
 *
 * Note that [ReadOnlyVar] dependency tracking is generally lazy. It will not find its dependencies until it is
 * [getOrCompute]d at least once.
 *
 * **Listeners:**
 * Listeners should be fired to signal that the [ReadOnlyVar] has changed in some way, including when invalidated.
 * They should not be fired again if the [ReadOnlyVar] was invalidated and then updated again because of that.
 */
interface ReadOnlyVar<out T> {

    @Suppress("NOTHING_TO_INLINE")
    companion object {

        /**
         * Returns a constant value [ReadOnlyVar]. The implementation used is memory optimized and doesn't
         * have dependencies like [GenericVar] would.
         */
        fun <T> const(value: T): ReadOnlyVar<T> = ReadOnlyConstVar(value)

        /**
         * Returns a constant value [ReadOnlyBooleanVar]. This directly calls [ReadOnlyBooleanVar.Companion.const].
         * @see ReadOnlyBooleanVar.Companion.const
         */
        inline fun const(value: Boolean): ReadOnlyBooleanVar = ReadOnlyBooleanVar.const(value)

        /**
         * Returns a constant value [ReadOnlyDoubleVar]. This directly calls [ReadOnlyDoubleVar.Companion.const].
         * @see ReadOnlyDoubleVar.Companion.const
         */
        inline fun const(value: Double): ReadOnlyDoubleVar = ReadOnlyDoubleVar.const(value)

        /**
         * Returns a constant value [ReadOnlyFloatVar]. This directly calls [ReadOnlyFloatVar.Companion.const].
         * @see ReadOnlyFloatVar.Companion.const
         */
        inline fun const(value: Float): ReadOnlyFloatVar = ReadOnlyFloatVar.const(value)

        /**
         * Returns a constant value [ReadOnlyIntVar]. This directly calls [ReadOnlyIntVar.Companion.const].
         * @see ReadOnlyIntVar.Companion.const
         */
        inline fun const(value: Int): ReadOnlyIntVar = ReadOnlyIntVar.const(value)

        /**
         * Returns a constant value [ReadOnlyLongVar]. This directly calls [ReadOnlyLongVar.Companion.const].
         * @see ReadOnlyLongVar.Companion.const
         */
        inline fun const(value: Long): ReadOnlyLongVar = ReadOnlyLongVar.const(value)

        /**
         * Returns a constant value [ReadOnlyCharVar]. This directly calls [ReadOnlyCharVar.Companion.const].
         * @see ReadOnlyCharVar.Companion.const
         */
        inline fun const(value: Char): ReadOnlyCharVar = ReadOnlyCharVar.const(value)
    }

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyVar].
     *
     * If using this [ReadOnlyVar] in a binding, use [VarContext] to do dependency tracking.
     */
    fun getOrCompute(): T

    /**
     * Adds a *strong* reference listener to this [ReadOnlyVar]. It can be removed with [removeListener].
     */
    fun addListener(listener: VarChangedListener<T>)

    /**
     * Adds a *strong* reference listener to this [ReadOnlyVar], and immediately fires it.
     * @see addListener
     */
    fun addListenerAndFire(listener: VarChangedListener<T>) {
        addListener(listener)
        listener.onChange(this)
    }

    /**
     * Removes the given [listener] from this [ReadOnlyVar].
     */
    fun removeListener(listener: VarChangedListener<T>)

    /**
     * Called to indicate that this var's contents are out of date. This should propagate a notification to listeners.
     * 
     * Should not do anything if already invalidated.
     */
    fun invalidate()

}
