package paintbox.binding


class FloatVarTests : AbstractVarTests<FloatVar, Float>() {

    override val varr: FloatVar = FloatVar(0.0f)

    override fun getConstant(): Float = 42.0f

    override fun getConstant2(): Float = 10.0f
}