package paintbox.binding


class BooleanVarTests : AbstractVarTests<BooleanVar, Boolean>() {

    override val varr: BooleanVar = BooleanVar(false)

    override fun getConstant(): Boolean = false

    override fun getConstant2(): Boolean = true
}