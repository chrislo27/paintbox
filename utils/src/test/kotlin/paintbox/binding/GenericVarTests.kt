package paintbox.binding


abstract class AbstractGenericVarTests<T> : AbstractVarTests<GenericVar<T>, T>()

class GenericVarTests : AbstractGenericVarTests<String>() {

    override val varr: GenericVar<String> = GenericVar("")

    override fun getConstant(): String = "Hello world!"

    override fun getConstant2(): String = "foo bar"
}
