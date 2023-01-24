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
     * If using this [ReadOnlyVar] in a binding, use [Var.Context] to do dependency tracking.
     */
    fun getOrCompute(): T

    /**
     * Adds a *strong* reference listener to this [ReadOnlyVar]. It can be removed with [removeListener].
     */
    fun addListener(listener: VarChangedListener<T>)

    /**
     * Removes the given [listener] from this [ReadOnlyVar].
     */
    fun removeListener(listener: VarChangedListener<T>)

    /**
     * Called by [InvalListener] to indicate that this var's contents are out of date.
     * Should not do anything if already invalidated.
     */
    fun invalidate()
    
}

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
        fun <T> bind(computation: Context.() -> T): Var<T> = GenericVar(computation)
        
        /**
         * Creates a [GenericVar] eagerly bound to the given [computation]. 
         * @see Var.eagerBind
         */
        fun <T> eagerBind(computation: Context.() -> T): Var<T> = GenericVar(eager = true, computation)
        
        /**
         * Creates a [GenericVar] bound to the given [computation].
         * 
         * This is identical to the [bind][Companion.bind] function.
         * @see Var.bind
         */
        operator fun <T> invoke(computation: Context.() -> T): Var<T> = bind(computation)

        /**
         * Creates a [GenericVar] with the given [item] as the base value and a [sideEffecting] function. 
         * @see Var.sideEffecting
         * @see Var.sideEffectingAndRetain
         */
        fun <T> sideEffecting(item: T, sideEffecting: Context.(existing: T) -> T): Var<T> = GenericVar(item, sideEffecting)

        
        // Warnings for specialized versions of plain invoke -----------------------------------------------------------
        
        @Deprecated("Prefer using the FloatVar constructor to avoid confusion with generic versions",
                replaceWith = ReplaceWith("FloatVar"),
                level = DeprecationLevel.ERROR)
        operator fun invoke(item: Float): FloatVar = FloatVar(item)
        
        @Deprecated("Prefer using the BooleanVar constructor to avoid confusion with generic versions",
                replaceWith = ReplaceWith("BooleanVar"),
                level = DeprecationLevel.ERROR)
        operator fun invoke(item: Boolean): BooleanVar = BooleanVar(item)
        
        @Deprecated("Prefer using the IntVar constructor to avoid confusion with generic versions",
                replaceWith = ReplaceWith("IntVar"),
                level = DeprecationLevel.ERROR)
        operator fun invoke(item: Int): IntVar = IntVar(item)
        
        @Deprecated("Prefer using the LongVar constructor to avoid confusion with generic versions",
                replaceWith = ReplaceWith("LongVar"),
                level = DeprecationLevel.ERROR)
        operator fun invoke(item: Long): LongVar = LongVar(item)
        
        @Deprecated("Prefer using the DoubleVar constructor to avoid confusion with generic versions",
                replaceWith = ReplaceWith("DoubleVar"),
                level = DeprecationLevel.ERROR)
        operator fun invoke(item: Double): DoubleVar = DoubleVar(item)
        
        @Deprecated("Prefer using the CharVar constructor to avoid confusion with generic versions",
                replaceWith = ReplaceWith("CharVar"),
                level = DeprecationLevel.ERROR)
        operator fun invoke(item: Char): CharVar = CharVar(item)
        
    }

    /**
     * Sets this [Var] to be the value of [item].
     */
    fun set(item: T)

    /**
     * Binds this [Var] to be computed from [computation]. The computation can depend
     * on other [ReadOnlyVar]s by calling [Context.use].
     * 
     * @see eagerBind
     */
    fun bind(computation: Context.() -> T)
    
    /**
     * Binds this [Var] to be computed from [computation], exactly like [bind] would.
     * 
     * This function will call [getOrCompute] immediately after binding, which is useful
     * to make sure dependencies from the binding are registered immediately and not lazily.
     * 
     * @see bind
     */
    fun eagerBind(computation: Context.() -> T): T {
        bind(computation)
        return getOrCompute()
    }

    /**
     * Sets this var to be the value of [item] while being updated/mutated by [sideEffecting].
     * 
     * @see sideEffectingAndRetain
     */
    fun sideEffecting(item: T, sideEffecting: Context.(existing: T) -> T)

    /**
     * Sets this var to be updated/mutated
     * by [sideEffecting], while *retaining* the existing value gotten from [getOrCompute].
     * 
     * @see sideEffecting
     */
    fun sideEffectingAndRetain(sideEffecting: Context.(existing: T) -> T) {
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
     * If using this [Var] in a binding, use [Var.Context] to do dependency tracking.
     */
    override fun getOrCompute(): T

    class Context {
        
        val dependencies: MutableSet<ReadOnlyVar<Any?>> = LinkedHashSet(2)
        
        
        /**
         * Adds the [varr] as a dependency and returns the var's value.
         * 
         * This function can be used on any [ReadOnlyVar] (even non-specialized ones) for compatibility. It is
         * recommended to use the receiver-style `use` function where possible.
         * 
         * Note that if the receiver is a primitive-specialized var,
         * the appropriate specialized `use` function should be used instead.
         */
        @JvmName("use")
        fun <R> use(varr: ReadOnlyVar<R>): R { // DON'T add specialized deprecations for this particular use function for generic compatibility
            dependencies += varr
            return varr.getOrCompute()
        }

        /**
         * Adds the receiver [var][R] as a dependency and returns the var's value.
         * 
         * This is the "receiver-style" syntax for more fluent usage.
         * Note that if the receiver is a primitive-specialized var,
         * the appropriate specialized `use` function should be used instead.
         */
        @JvmName("useAndGet")
        fun <R> ReadOnlyVar<R>.use(): R { // Specialized deprecations may be added for this use function
            return use(this)
        }
        
        
        //region Specialization methods

        @Deprecated("Don't use ReadOnlyVar<Float>, use ReadOnlyFloatVar.use() instead to avoid explicit boxing",
                replaceWith = ReplaceWith("(this as ReadOnlyFloatVar).use()"),
                level = DeprecationLevel.ERROR)
        fun ReadOnlyVar<Float>.use(): Float {
            return use(this)
        }

        /**
         * The float specialization method. Adds the receiver as a dependency and returns a primitive float.
         */
        fun ReadOnlyFloatVar.use(): Float {
            dependencies += this
            return this.get()
        }
        

        @Deprecated("Don't use ReadOnlyVar<Boolean>, use ReadOnlyBooleanVar.use() instead to avoid explicit boxing",
                replaceWith = ReplaceWith("(this as ReadOnlyBooleanVar).use()"),
                level = DeprecationLevel.ERROR)
        fun ReadOnlyVar<Boolean>.use(): Boolean {
            return use(this)
        }

        /**
         * The boolean specialization method. Adds the receiver as a dependency and returns a primitive boolean.
         */
        fun ReadOnlyBooleanVar.use(): Boolean {
            dependencies += this
            return this.get()
        }
        

        @Deprecated("Don't use ReadOnlyVar<Int>, use ReadOnlyIntVar.use() instead to avoid explicit boxing",
                replaceWith = ReplaceWith("(this as ReadOnlyIntVar).use()"),
                level = DeprecationLevel.ERROR)
        fun ReadOnlyVar<Int>.use(): Int {
            return use(this)
        }

        /**
         * The int specialization method. Adds the receiver as a dependency and returns a primitive int.
         */
        fun ReadOnlyIntVar.use(): Int {
            dependencies += this
            return this.get()
        }
        

        @Deprecated("Don't use ReadOnlyVar<Long>, use ReadOnlyLongVar.use() instead to avoid explicit boxing",
                replaceWith = ReplaceWith("(this as ReadOnlyLongVar).use()"),
                level = DeprecationLevel.ERROR)
        fun ReadOnlyVar<Long>.use(): Long {
            return use(this)
        }

        /**
         * The long specialization method. Adds the receiver as a dependency and returns a primitive long.
         */
        fun ReadOnlyLongVar.use(): Long {
            dependencies += this
            return this.get()
        }
        

        @Deprecated("Don't use ReadOnlyVar<Double>, use ReadOnlyDoubleVar.use() instead to avoid explicit boxing",
                replaceWith = ReplaceWith("(this as ReadOnlyDoubleVar).use()"),
                level = DeprecationLevel.ERROR)
        fun ReadOnlyVar<Double>.use(): Double {
            return use(this)
        }

        /**
         * The double specialization method. Adds the receiver as a dependency and returns a primitive double.
         */
        fun ReadOnlyDoubleVar.use(): Double {
            dependencies += this
            return this.get()
        }
        

        @Deprecated("Don't use ReadOnlyVar<Char>, use ReadOnlyCharVar.use() instead to avoid explicit boxing",
                replaceWith = ReplaceWith("(this as ReadOnlyCharVar).use()"),
                level = DeprecationLevel.ERROR)
        fun ReadOnlyVar<Char>.use(): Char {
            return use(this)
        }

        /**
         * The char specialization method. Adds the receiver as a dependency and returns a primitive char.
         */
        fun ReadOnlyCharVar.use(): Char {
            dependencies += this
            return this.get()
        }
        
        //endregion
    }
}
