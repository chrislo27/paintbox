package paintbox.binding


/**
 * An abstract class that implements [ReadOnlyVar]'s listener and invalidation support.
 */
abstract class ReadOnlyVarBase<T> : ReadOnlyVar<T> {

    companion object {

        private fun <T> createMutableSet(initialCapacity: Int): MutableSet<T> = LinkedHashSet(initialCapacity)
    }

    protected var invalidated: Boolean = true
    private var isNotifyingListeners: Boolean = false
    private var listeners: MutableSet<VarChangedListener<T>> = createMutableSet(4)

    protected open fun notifyListeners() {
        val shouldChangeFlag = !isNotifyingListeners

        if (shouldChangeFlag) isNotifyingListeners = true

        var anyNeedToBeDisposed = 0
        listeners.forEach { l ->
            l.onChange(this)
            if (l is DisposableVarChangedListener<*> && l.shouldBeDisposed) {
                anyNeedToBeDisposed += 1
            }
        }

        if (shouldChangeFlag) isNotifyingListeners = false

        if (anyNeedToBeDisposed > 0) {
            if (isNotifyingListeners) {
                val ls = listeners
                listeners = ls.filterNotTo(LinkedHashSet(ls.size - anyNeedToBeDisposed)) { l ->
                    l is DisposableVarChangedListener<*> && l.shouldBeDisposed
                }
            } else {
                // Safe to iterate through listeners, no one else in this thread is using it because isNotifyingListeners == false
                listeners.removeIf { l -> l is DisposableVarChangedListener<*> && l.shouldBeDisposed }
            }
        }
    }

    override fun addListener(listener: VarChangedListener<T>) {
        if (listener !in listeners) {
            if (isNotifyingListeners) {
                copyListenersWith(listener)
            } else {
                listeners.add(listener)
            }
        }
    }

    override fun removeListener(listener: VarChangedListener<T>) {
        if (listener in listeners) {
            if (isNotifyingListeners) {
                copyListenersWithout(listener)
            } else {
                listeners.remove(listener)
            }
        }
    }

    override fun invalidate() {
        if (!this.invalidated) {
            this.invalidated = true
            this.notifyListeners()
        }
    }

    private fun copyListenersWith(listener: VarChangedListener<T>) {
        listeners = LinkedHashSet<VarChangedListener<T>>(listeners.size + 1).apply {
            this.addAll(listeners)
            this.add(listener)
        }
    }

    private fun copyListenersWithout(listener: VarChangedListener<T>) {
        listeners = createMutableSet<VarChangedListener<T>>(listeners.size - 1).apply {
            var removed = false
            listeners.forEach { l ->
                if (!removed && l == listener) {
                    removed = true
                } else {
                    this.add(l)
                }
            }
        }
    }
}
