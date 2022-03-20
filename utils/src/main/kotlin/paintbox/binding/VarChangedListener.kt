package paintbox.binding


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
 */
interface DisposableVarChangedListener<in T> : VarChangedListener<T> {
    /**
     * True when this listener should be disposed.
     * Once this is true, it shall never be false on subsequent gets.
     */
    val shouldBeDisposed: Boolean
}
