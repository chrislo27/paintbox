package paintbox.binding


/**
 * An abstract class that implements [ReadOnlyVar]'s listener and invalidation support.
 */
abstract class ReadOnlyVarBase<T> : ReadOnlyVar<T> {
    
    protected var invalidated: Boolean = true
    protected var listeners: Set<VarChangedListener<T>> = emptySet()

    protected open fun notifyListeners() {
        var anyNeedToBeDisposed = 0
        val ls = listeners
        ls.forEach { l ->
            l.onChange(this)
            if (l is DisposableVarChangedListener<*> && l.shouldBeDisposed) {
                anyNeedToBeDisposed += 1
            }
        }
        if (anyNeedToBeDisposed > 0) {
            listeners = ls.filterNotTo(LinkedHashSet(ls.size - anyNeedToBeDisposed)) { l ->
                l is DisposableVarChangedListener<*> && l.shouldBeDisposed
            }
        }
    }
    
    override fun addListener(listener: VarChangedListener<T>) {
        if (listener !in listeners) {
            listeners = listeners + listener
        }
    }

    override fun removeListener(listener: VarChangedListener<T>) {
        if (listener in listeners) {
            listeners = listeners - listener
        }
    }

    override fun invalidate() {
        if (!this.invalidated) {
            this.invalidated = true
            this.notifyListeners()
        }
    }
}
