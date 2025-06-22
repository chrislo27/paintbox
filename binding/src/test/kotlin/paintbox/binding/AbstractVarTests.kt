package paintbox.binding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


abstract class AbstractVarTests<V, T> where V : Var<T> {
    
    
    abstract fun createVar(): V
    abstract fun getConstant(): T
    abstract fun getConstant2(): T
    
    val varr: V = createVar()
    
    @Test
    fun `set stores a value`() {
        // Arrange
        val constant = getConstant()
        varr.set(constant)
        
        // Act
        val result = varr.getOrCompute()
        
        // Assert
        assertEquals(constant, result)
    }
    
    @Test
    fun `bind calls the computation`() {
        // Arrange
        val constant = getConstant()
        var flag = false
        varr.bind { 
            flag = true
            constant
        }
        
        // Act
        val result = varr.getOrCompute()
        
        // Assert
        assertEquals(constant, result)
        assertTrue(flag)
        assertEquals(constant, varr.getOrCompute()) // Second call is cached
    }
    
    @Test
    fun `bind(ReadOnlyVar) binds to the given var`() {
        // Arrange
        val other = createVar()
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

    @Test
    fun `binding to another ReadOnlyVar of the same type or specialization doesn't rediscover dependencies when invalidated`() {
        // Arrange
        val other = createVar()
        val constant = getConstant()
        other.set(constant)

        varr.bind(other)

        // Check if dependencies were reloaded in varr by using reflection to check dependencies object
        val dependenciesField = varr::class.java.getDeclaredField("dependencies")
        dependenciesField.isAccessible = true
        
        val dependenciesBefore = dependenciesField.get(varr) as Set<*>
        
        // Act
        other.set(getConstant2())

        // Assert
        val dependenciesAfter = dependenciesField.get(varr) as Set<*>
        assertTrue(dependenciesBefore === dependenciesAfter)
        assertTrue(other in dependenciesAfter)
        assertEquals(1, dependenciesAfter.size)
    }
    
    @Test
    fun `sideEffecting calls the computation`() {
        // Arrange
        val constant = getConstant()
        val constant2 = getConstant2()
        var flag = false
        var passedInValue: T? = null
        varr.sideEffecting(constant) { 
            flag = true
            passedInValue = it
            constant2
        }
        
        // Act
        val result = varr.getOrCompute()
        
        // Assert
        assertNotEquals(constant, constant2)
        assertEquals(constant, passedInValue)
        assertEquals(constant2, result)
        assertTrue(flag)
    }

    @Test
    fun `bind gets called twice for invalidation`() {
        // Arrange
        val invalidatableVar = InvalidatableVar()
        
        var bindCounts = 0
        varr.bind {
            bindCounts++
            invalidatableVar.use()
            getConstant()
        }

        // Act & Assert
        varr.getOrCompute()
        assertEquals(1, bindCounts)
        
        // Calling again should be cached
        varr.getOrCompute()
        assertEquals(1, bindCounts)
        
        // Invalidation shouldn't trigger a bind recomputation immediately (should be lazy)
        invalidatableVar.invalidate()
        assertEquals(1, bindCounts)
        
        varr.getOrCompute()
        assertEquals(2, bindCounts)
    }

    @Test
    fun `sideEffecting gets called twice for invalidation`() {
        // Arrange
        val invalidatableVar = InvalidatableVar()
        
        var bindCounts = 0
        varr.sideEffecting(getConstant()) {
            bindCounts++
            invalidatableVar.use()
            getConstant()
        }

        // Act & Assert
        varr.getOrCompute()
        assertEquals(1, bindCounts)
        
        // Calling again should be cached
        varr.getOrCompute()
        assertEquals(1, bindCounts)
        
        // Invalidation shouldn't trigger a bind recomputation immediately (should be lazy)
        invalidatableVar.invalidate()
        assertEquals(1, bindCounts)
        
        varr.getOrCompute()
        assertEquals(2, bindCounts)
    }

    @Test
    fun `invalidation notifies listeners only once`() {
        // Arrange
        val listener = NotifyListener()
        
        varr.addListener(listener)
        varr.bind {
            getConstant()
        }

        // Act
        varr.invalidate()
        varr.invalidate()
        
        // Assert
        assertEquals(1, listener.notifyCount)
    }

    @Test
    fun `addListener registers the listener`() {
        // Arrange
        val listener = NotifyListener()
        varr.bind {
            getConstant()
        }
        varr.getOrCompute()

        // Act
        varr.addListener(listener)
        varr.invalidate()
        
        // Assert
        assertEquals(1, listener.notifyCount)
    }

    @Test
    fun `removeListener unregisters the listener`() {
        // Arrange
        val listener = NotifyListener()
        varr.bind {
            getConstant()
        }
        varr.getOrCompute()

        // Act
        varr.addListener(listener)
        varr.invalidate()
        
        varr.removeListener(listener)
        varr.getOrCompute()
        varr.invalidate()
        
        // Assert
        assertEquals(1, listener.notifyCount)
    }

    @Test
    fun `binding and re-binding correctly adds and removes invalidation listeners`() {
        // Arrange
        val dependency1 = InvalidatableVar()
        val dependency2 = InvalidatableVar()
        
        // Act & Assert
        varr.bind {
            dependency1.use()
            getConstant()
        }
        varr.getOrCompute()
        
        assertEquals(1, dependency1.listeners.size)
        assertEquals(0, dependency2.listeners.size)
        
        
        varr.bind {
            dependency2.use()
            getConstant2()
        }
        varr.getOrCompute()
        
        assertEquals(1, dependency2.listeners.size)
        assertEquals(0, dependency1.listeners.size)
    }

    @Test
    fun `sideEffecting and re-sideEffecting correctly adds and removes invalidation listeners`() {
        // Arrange
        val dependency1 = InvalidatableVar()
        val dependency2 = InvalidatableVar()
        
        // Act & Assert
        varr.sideEffecting(getConstant2()) {
            dependency1.use()
            getConstant()
        }
        varr.getOrCompute()
        
        assertEquals(1, dependency1.listeners.size)
        assertEquals(0, dependency2.listeners.size)
        
        
        varr.sideEffecting(getConstant()) {
            dependency2.use()
            getConstant2()
        }
        varr.getOrCompute()
        
        assertEquals(1, dependency2.listeners.size)
        assertEquals(0, dependency1.listeners.size)
    }
    
    private class InvalidatableVar : ReadOnlyVar<Unit> {

        var invalidated = false
        val listeners: MutableSet<VarChangedListener<Unit>> = mutableSetOf()

        override fun getOrCompute() {
            invalidated = false
        }

        override fun addListener(listener: VarChangedListener<Unit>) {
            listeners += listener
        }

        override fun removeListener(listener: VarChangedListener<Unit>) {
            listeners -= listener
        }

        override fun invalidate() {
            if (!invalidated) {
                invalidated = true
                listeners.forEach { it.onChange(this) }
            }
        }
    }
    
    private inner class NotifyListener : VarChangedListener<T> {

        var notifyCount = 0
        
        override fun onChange(v: ReadOnlyVar<T>) {
            notifyCount++
        }
    }
} 