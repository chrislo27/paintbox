package paintbox.binding


class LongVarTests : SpecializedVarBaseTests<LongVar, Long>() {

    override fun createVar(): LongVar {
        return LongVar(0L)
    }

    override fun getConstant(): Long = 42L

    override fun getConstant2(): Long = Long.MAX_VALUE
}