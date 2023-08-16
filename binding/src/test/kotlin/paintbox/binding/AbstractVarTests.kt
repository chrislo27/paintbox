package paintbox.binding

import kotlin.test.*


abstract class AbstractVarTests<V, T>
        where V : Var<T> {
    
    abstract val varr: V
    
    abstract fun getConstant(): T
    abstract fun getConstant2(): T
    
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
    
    private class InvalidatableVar : ReadOnlyVar<Unit> {

        var invalidated = false
        private val listeners: MutableSet<VarChangedListener<Unit>> = mutableSetOf()

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