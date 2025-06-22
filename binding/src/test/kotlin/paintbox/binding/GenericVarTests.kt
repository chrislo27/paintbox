package paintbox.binding


abstract class AbstractGenericVarTests<T> : AbstractVarTests<GenericVar<T>, T>()

class GenericVarTests : AbstractGenericVarTests<String>() {

    override fun createVar(): GenericVar<String> {
        return GenericVar("")
    }

    override fun getConstant(): String = "Hello world!"

    override fun getConstant2(): String = "foo bar"
}
