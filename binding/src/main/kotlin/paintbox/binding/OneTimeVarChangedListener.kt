package paintbox.binding


/**
 * A [VarChangedListener] that can only listen to an [onChange] event once. Once it has listened
 * to an event, it will not listen to other events, and will be marked for disposal.
 */
class OneTimeVarChangedListener<in T>(private val listener: VarChangedListener<T>) : DisposableVarChangedListener<T> {

    override var shouldBeDisposed: Boolean = false
        private set
    
    override fun onChange(v: ReadOnlyVar<T>) {
        if (!shouldBeDisposed) {
            shouldBeDisposed = true
            listener.onChange(v)
        }
    }
}
