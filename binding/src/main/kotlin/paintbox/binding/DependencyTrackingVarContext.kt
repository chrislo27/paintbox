package paintbox.binding


/**
 * A [VarContext] that tracks the bound [ReadOnlyVar]s as they are bound.
 */
class DependencyTrackingVarContext : VarContext {

    private val _dependencies: MutableSet<ReadOnlyVar<Any?>> = LinkedHashSet(2)
    val dependencies: Set<ReadOnlyVar<Any?>> get() = _dependencies

    override fun <R> bindAndGet(varr: ReadOnlyVar<R>): R {
        _dependencies += varr
        return varr.getOrCompute()
    }

    override fun ReadOnlyFloatVar.use(): Float {
        _dependencies += this
        return this.get()
    }

    override fun ReadOnlyBooleanVar.use(): Boolean {
        _dependencies += this
        return this.get()
    }

    override fun ReadOnlyIntVar.use(): Int {
        _dependencies += this
        return this.get()
    }

    override fun ReadOnlyLongVar.use(): Long {
        _dependencies += this
        return this.get()
    }

    override fun ReadOnlyDoubleVar.use(): Double {
        _dependencies += this
        return this.get()
    }

    override fun ReadOnlyCharVar.use(): Char {
        _dependencies += this
        return this.get()
    }
}
