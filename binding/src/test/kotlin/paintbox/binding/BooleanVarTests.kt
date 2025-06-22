package paintbox.binding


class BooleanVarTests : AbstractVarTests<BooleanVar, Boolean>() {

    override fun createVar(): BooleanVar {
        return BooleanVar(false)
    }

    override fun getConstant(): Boolean = false

    override fun getConstant2(): Boolean = true
}