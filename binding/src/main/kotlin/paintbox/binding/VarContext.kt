package paintbox.binding


class VarContext {

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
    fun <R> use(varr: ReadOnlyVar<R>): R { // DON'T add specialized deprecations for this particular `use` function for generic compatibility
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

    @Deprecated(
        "Don't use ReadOnlyVar<Float>, use ReadOnlyFloatVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyFloatVar).use()"),
        level = DeprecationLevel.ERROR
    )
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


    @Deprecated(
        "Don't use ReadOnlyVar<Boolean>, use ReadOnlyBooleanVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyBooleanVar).use()"),
        level = DeprecationLevel.ERROR
    )
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


    @Deprecated(
        "Don't use ReadOnlyVar<Int>, use ReadOnlyIntVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyIntVar).use()"),
        level = DeprecationLevel.ERROR
    )
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


    @Deprecated(
        "Don't use ReadOnlyVar<Long>, use ReadOnlyLongVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyLongVar).use()"),
        level = DeprecationLevel.ERROR
    )
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


    @Deprecated(
        "Don't use ReadOnlyVar<Double>, use ReadOnlyDoubleVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyDoubleVar).use()"),
        level = DeprecationLevel.ERROR
    )
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


    @Deprecated(
        "Don't use ReadOnlyVar<Char>, use ReadOnlyCharVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyCharVar).use()"),
        level = DeprecationLevel.ERROR
    )
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