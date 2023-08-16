package paintbox.binding


class DoubleVarTests : AbstractVarTests<DoubleVar, Double>() {

    override val varr: DoubleVar = DoubleVar(0.0)

    override fun getConstant(): Double = 42.0

    override fun getConstant2(): Double = 12.0
}