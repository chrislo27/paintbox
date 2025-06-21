package paintbox.binding

import paintbox.binding.Var.Companion.bind
import paintbox.binding.Var.Companion.eagerBind
import paintbox.binding.Var.Companion.of
import paintbox.binding.Var.Companion.sideEffecting



/**
 * Represents a mutable var.
 *
 * The default implementation is [GenericVar].
 */
interface Var<T> : ReadOnlyVar<T> {

    /**
     * These are default "constructor functions" that will use [GenericVar] as its implementation.
     */
    companion object {

        /**
         * Creates a [GenericVar] with the given [item] as a constant value.
         * @see Var.set
         */
        fun <T> of(item: T): Var<T> = GenericVar(item)

        /**
         * Creates a [GenericVar] with the given [item] as a constant value.
         * @see Var.set
         * @see of
         */
        operator fun <T> invoke(item: T): Var<T> = of(item)

        /**
         * Creates a [GenericVar] bound to the given [computation].
         * @see Var.bind
         */
        fun <T> bind(computation: ContextBinding<T>): Var<T> = GenericVar(computation)

        /**
         * Creates a [GenericVar] eagerly bound to the given [computation].
         * @see Var.eagerBind
         */
        fun <T> eagerBind(computation: ContextBinding<T>): Var<T> = GenericVar(eager = true, computation)

        /**
         * Creates a [GenericVar] bound to the given [computation].
         *
         * This is identical to the [bind][Companion.bind] function.
         * @see Var.bind
         */
        operator fun <T> invoke(computation: ContextBinding<T>): Var<T> = bind(computation)

        /**
         * Creates a [GenericVar] with the given [item] as the base value and a [sideEffecting] function.
         * @see Var.sideEffecting
         * @see Var.sideEffectingAndRetain
         */
        fun <T> sideEffecting(item: T, sideEffecting: ContextSideEffecting<T>): Var<T> = 
            GenericVar(item, sideEffecting)


        //region Warnings for specialized versions of plain invoke

        @Deprecated(
            "Prefer using the FloatVar constructor to avoid confusion with generic versions",
            replaceWith = ReplaceWith("FloatVar"),
            level = DeprecationLevel.ERROR
        )
        operator fun invoke(item: Float): FloatVar = FloatVar(item)

        @Deprecated(
            "Prefer using the BooleanVar constructor to avoid confusion with generic versions",
            replaceWith = ReplaceWith("BooleanVar"),
            level = DeprecationLevel.ERROR
        )
        operator fun invoke(item: Boolean): BooleanVar = BooleanVar(item)

        @Deprecated(
            "Prefer using the IntVar constructor to avoid confusion with generic versions",
            replaceWith = ReplaceWith("IntVar"),
            level = DeprecationLevel.ERROR
        )
        operator fun invoke(item: Int): IntVar = IntVar(item)

        @Deprecated(
            "Prefer using the LongVar constructor to avoid confusion with generic versions",
            replaceWith = ReplaceWith("LongVar"),
            level = DeprecationLevel.ERROR
        )
        operator fun invoke(item: Long): LongVar = LongVar(item)

        @Deprecated(
            "Prefer using the DoubleVar constructor to avoid confusion with generic versions",
            replaceWith = ReplaceWith("DoubleVar"),
            level = DeprecationLevel.ERROR
        )
        operator fun invoke(item: Double): DoubleVar = DoubleVar(item)

        @Deprecated(
            "Prefer using the CharVar constructor to avoid confusion with generic versions",
            replaceWith = ReplaceWith("CharVar"),
            level = DeprecationLevel.ERROR
        )
        operator fun invoke(item: Char): CharVar = CharVar(item)

        //endregion
    }

    /**
     * Sets this [Var] to be the value of [item].
     */
    fun set(item: T)

    /**
     * Binds this [Var] to be computed from [computation]. The computation can depend
     * on other [ReadOnlyVar]s by calling [VarContext.use].
     *
     * @see eagerBind
     */
    fun bind(computation: ContextBinding<T>)

    /**
     * Binds this [Var] to be computed from [computation], exactly like [bind] would.
     *
     * This function will call [getOrCompute] immediately after binding, which is useful
     * to make sure dependencies from the binding are registered immediately and not lazily.
     *
     * @see bind
     */
    fun eagerBind(computation: ContextBinding<T>): T {
        bind(computation)
        return getOrCompute()
    }

    /**
     * Directly binds this var to the same value as the given [readOnlyVar]. This has the benefit of ensuring that
     * the only dependency for this [Var] is the given [readOnlyVar].
     * 
     * The default implementation of this function simply calls the [ContextBinding] overload of [bind].
     * Implementations of this interface should override this with an optimized implementation
     * that avoids recalculating dependencies.
     */
    fun bind(readOnlyVar: ReadOnlyVar<T>) {
        this.bind({ readOnlyVar.use() })
    }

    /**
     * Sets this var to be the value of [item] while being updated/mutated by [sideEffecting].
     *
     * @see sideEffectingAndRetain
     */
    fun sideEffecting(item: T, sideEffecting: ContextSideEffecting<T>)

    /**
     * Sets this var to be updated/mutated
     * by [sideEffecting], while *retaining* the existing value gotten from [getOrCompute].
     *
     * @see sideEffecting
     */
    fun sideEffectingAndRetain(sideEffecting: ContextSideEffecting<T>) {
        sideEffecting(getOrCompute(), sideEffecting)
    }

    /**
     * Returns this [Var] as a [ReadOnlyVar]. Useful to avoid casting or explicit type definitions for a variable.
     *
     * Primitive specializations will return their primitive read only var type.
     */
    fun asReadOnlyVar(): ReadOnlyVar<T> = this

    // Javadoc override
    /**
     * Gets (and computes if necessary) the value represented by this [Var].
     *
     * If using this [Var] in a binding, use [VarContext] to do dependency tracking.
     */
    override fun getOrCompute(): T

}
