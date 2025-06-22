package paintbox.binding


class DoubleVarTests : SpecializedVarBaseTests<DoubleVar, Double>() {

    override fun createVar(): DoubleVar {
        return DoubleVar(0.0)
    }

    override fun getConstant(): Double = 42.0

    override fun getConstant2(): Double = 12.0
}