package paintbox.binding


class IntVarTests : AbstractVarTests<IntVar, Int>() {

    override val varr: IntVar = IntVar(0)

    override fun getConstant(): Int = 42

    override fun getConstant2(): Int = 12
}