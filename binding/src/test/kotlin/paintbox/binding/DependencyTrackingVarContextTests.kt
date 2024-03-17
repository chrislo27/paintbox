package paintbox.binding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class DependencyTrackingVarContextTests {
    
    @Test
    fun `use adds a dependency`() {
        // Arrange
        val context = DependencyTrackingVarContext()
        val varr: ReadOnlyVar<*> = UnitReadOnlyVar()
        
        // Act
        context.bindAndGet(varr)
        
        // Assert
        assertEquals(1, context.dependencies.size)
        assertEquals(varr, context.dependencies.first())
    }
    
    @Test
    fun `use calls getOrCompute`() {
        // Arrange
        val context = DependencyTrackingVarContext()
        val varr = UnitReadOnlyVar()
        
        // Act
        context.bindAndGet(varr)
        
        // Assert
        assertTrue(varr.getOrComputeCalled)
    }
    
    private class UnitReadOnlyVar : ReadOnlyVar<Unit> {
        
        var getOrComputeCalled: Boolean = false

        override fun getOrCompute() {
            getOrComputeCalled = true
        }

        override fun addListener(listener: VarChangedListener<Unit>) {
        }

        override fun removeListener(listener: VarChangedListener<Unit>) {
        }

        override fun invalidate() {
            getOrComputeCalled = false
        }
    }
}