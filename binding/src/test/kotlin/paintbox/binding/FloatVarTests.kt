package paintbox.binding


class FloatVarTests : SpecializedVarBaseTests<FloatVar, Float>() {
    
    override fun createVar(): FloatVar {
        return FloatVar(0.0f)
    }

    override fun getConstant(): Float = 42.0f

    override fun getConstant2(): Float = 10.0f
}