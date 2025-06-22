package paintbox.binding


class CharVarTests : AbstractVarTests<CharVar, Char>() {

    override fun createVar(): CharVar {
        return CharVar('a')
    }

    override fun getConstant(): Char = 'e'

    override fun getConstant2(): Char = '1'
}