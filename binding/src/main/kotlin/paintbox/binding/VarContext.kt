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
    fun <R> bindAndGet(varr: ReadOnlyVar<R>): R { // NB: DON'T add specialized deprecations for this non-receiver-type function
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
    fun <R> ReadOnlyVar<R>.use(): R { // Specializations should be added for this receiver-type function
        return bindAndGet(this)
    }


    //region Specialization methods

    @Deprecated(
        "If this is a ReadOnlyFloatVar, use ReadOnlyFloatVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyFloatVar).use()"),
        level = DeprecationLevel.WARNING
    )
    fun ReadOnlyVar<Float>.use(): Float {
        return bindAndGet(this)
    }

    /**
     * The float specialization method. Adds the receiver as a dependency and returns a primitive float.
     */
    fun ReadOnlyFloatVar.use(): Float {
        dependencies += this
        return this.get()
    }


    @Deprecated(
        "If this is a ReadOnlyBooleanVar, use ReadOnlyBooleanVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyBooleanVar).use()"),
        level = DeprecationLevel.WARNING
    )
    fun ReadOnlyVar<Boolean>.use(): Boolean {
        return bindAndGet(this)
    }

    /**
     * The boolean specialization method. Adds the receiver as a dependency and returns a primitive boolean.
     */
    fun ReadOnlyBooleanVar.use(): Boolean {
        dependencies += this
        return this.get()
    }


    @Deprecated(
        "If this is aReadOnlyIntVar, use ReadOnlyIntVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyIntVar).use()"),
        level = DeprecationLevel.WARNING
    )
    fun ReadOnlyVar<Int>.use(): Int {
        return bindAndGet(this)
    }

    /**
     * The int specialization method. Adds the receiver as a dependency and returns a primitive int.
     */
    fun ReadOnlyIntVar.use(): Int {
        dependencies += this
        return this.get()
    }


    @Deprecated(
        "If this is a ReadOnlyLongVar, use ReadOnlyLongVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyLongVar).use()"),
        level = DeprecationLevel.WARNING
    )
    fun ReadOnlyVar<Long>.use(): Long {
        return bindAndGet(this)
    }

    /**
     * The long specialization method. Adds the receiver as a dependency and returns a primitive long.
     */
    fun ReadOnlyLongVar.use(): Long {
        dependencies += this
        return this.get()
    }


    @Deprecated(
        "If this is a ReadOnlyDoubleVar, use ReadOnlyDoubleVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyDoubleVar).use()"),
        level = DeprecationLevel.WARNING
    )
    fun ReadOnlyVar<Double>.use(): Double {
        return bindAndGet(this)
    }

    /**
     * The double specialization method. Adds the receiver as a dependency and returns a primitive double.
     */
    fun ReadOnlyDoubleVar.use(): Double {
        dependencies += this
        return this.get()
    }


    @Deprecated(
        "If this is a ReadOnlyCharVar, use ReadOnlyCharVar.use() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("(this as ReadOnlyCharVar).use()"),
        level = DeprecationLevel.WARNING
    )
    fun ReadOnlyVar<Char>.use(): Char {
        return bindAndGet(this)
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