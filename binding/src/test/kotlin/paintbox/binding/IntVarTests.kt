package paintbox.binding


class IntVarTests : SpecializedVarBaseTests<IntVar, Int>() {

    override fun createVar(): IntVar {
        return IntVar(0)
    }

    override fun getConstant(): Int = 42

    override fun getConstant2(): Int = 12
}