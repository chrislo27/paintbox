package paintbox.binding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


abstract class SpecializedVarBaseTests<V, T> : AbstractVarTests<V, T>() where V : SpecializedVar<T> {

    @Test
    fun `bind(ReadOnlyVar) using non-specialized var still binds normally`() {
        // Arrange
        val other: Var<T> = GenericVar(getConstant2())
        val constant = getConstant()
        var flag = false
        other.bind {
            flag = true
            constant
        }

        // Act
        varr.bind(other)
        val result = varr.getOrCompute()

        // Assert
        assertEquals(constant, result)
        assertTrue(flag)
        assertEquals(constant, varr.getOrCompute()) // Second call is cached
    }
}