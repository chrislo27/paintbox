package paintbox.binding

import java.lang.ref.WeakReference


/**
 * A listener that is fired when a [ReadOnlyVar] is changed or invalidated.
 * 
 * Listeners are not re-fired if they were previously fired on an invalidation event.
 * Specifically, the compute-type bindings do not trigger listeners when they are *actually* computed, just only when
 * their internal value has been invalidated.
 */
fun interface VarChangedListener<in T> {
    fun onChange(v: ReadOnlyVar<T>)
}

/**
 * A special kind of [VarChangedListener] that can be flagged as needing to be removed.
 * 
 * If all is needed is a weak reference wrapper around an existing [VarChangedListener], then
 * [WeakVarChangedListener] should be used instead of trying to implement this interface.
 * 
 * **Optimization:** Implementors of [ReadOnlyVar] will optimize for groups of [DisposableVarChangedListener] that need to be removed
 * simultaneously.
 */
interface DisposableVarChangedListener<in T> : VarChangedListener<T> {
    /**
     * True when this listener should be disposed.
     * Once this is true, it shall never be false on subsequent gets.
     */
    val shouldBeDisposed: Boolean
}

/**
 * A wrapper around a [VarChangedListener] that is weakly referent.
 * This [WeakVarChangedListener] will be removed from its parent [ReadOnlyVar] when
 * the original listener has been garbage collected (using [DisposableVarChangedListener]).
 * 
 * It is **strongly recommended** to keep a strong reference to the original listener or else it may be
 * garbage collected too early.
 * 
 * @see DisposableVarChangedListener
 */
class WeakVarChangedListener<in T>(listener: VarChangedListener<T>) : DisposableVarChangedListener<T> {

    private val weakRef: WeakReference<VarChangedListener<T>> = WeakReference(listener)
    
    override var shouldBeDisposed: Boolean = false
        private set
    
    override fun onChange(v: ReadOnlyVar<T>) {
        val l = weakRef.get()
        if (l != null) {
            l.onChange(v)
        } else {
            shouldBeDisposed = true
        }
    }
}
