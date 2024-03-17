package paintbox.binding


internal class DefaultVarContextImpl : VarContext {

    override val dependencies: MutableSet<ReadOnlyVar<Any?>> = LinkedHashSet(2)

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
