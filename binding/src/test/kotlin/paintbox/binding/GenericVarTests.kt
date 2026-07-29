package paintbox.binding

import kotlin.test.Test
import kotlin.test.assertEquals


abstract class AbstractGenericVarTests<T> : AbstractVarTests<GenericVar<T>, T>()

class GenericVarTests : AbstractGenericVarTests<String>() {

    override fun createVar(): GenericVar<String> {
        return GenericVar("")
    }

    override fun getConstant(): String = "Hello world!"

    override fun getConstant2(): String = "foo bar"
    

    @Test
    fun `addListenerAndFire registers the listener and then fires it immediately 2`() {
        // Arrange
        val listener = NotifyAndComputeListener()

        val firstName = createVar()
        val lastName = createVar()

        firstName.set(getConstant())
        lastName.set(getConstant2())

        varr.bind {
            "${firstName.use()} ${lastName.use()}"
        }
        varr.getOrCompute()

        // Act
        varr.addListenerAndFire(listener)
        assertEquals(1, listener.notifyCount)
        
        firstName.set(getConstant2())

        // Assert
        assertEquals(2, listener.notifyCount)
        assertEquals("${getConstant2()} ${getConstant2()}", varr.getOrCompute())
    }

    private class NotifyAndComputeListener : VarChangedListener<String> {

        var notifyCount = 0

        override fun onChange(v: ReadOnlyVar<String>) {
            notifyCount++
            v.getOrCompute()
        }
    }
}
