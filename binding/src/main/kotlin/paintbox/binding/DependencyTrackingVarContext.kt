package paintbox.binding


/**
 * A [VarContext] that tracks the bound [ReadOnlyVar]s as they are bound.
 */
class DependencyTrackingVarContext : VarContext {

    val dependencies: Set<ReadOnlyVar<Any?>>
        field: MutableSet<ReadOnlyVar<Any?>> = LinkedHashSet(2)

    override fun <R> bindAndGet(varr: ReadOnlyVar<R>): R {
        dependencies += varr
        return varr.getOrCompute()
    }

    override fun ReadOnlyFloatVar.use(): Float {
        dependencies += this
        return this.get()
    }

    override fun ReadOnlyBooleanVar.use(): Boolean {
        dependencies += this
        return this.get()
    }

    override fun ReadOnlyIntVar.use(): Int {
        dependencies += this
        return this.get()
    }

    override fun ReadOnlyLongVar.use(): Long {
        dependencies += this
        return this.get()
    }

    override fun ReadOnlyDoubleVar.use(): Double {
        dependencies += this
        return this.get()
    }

    override fun ReadOnlyCharVar.use(): Char {
        dependencies += this
        return this.get()
    }
}
