package paintbox.binding


fun interface VarChangedListener<in T> {
    fun onChange(v: ReadOnlyVar<T>)
}

interface DisposableVarChangedListener<in T> : VarChangedListener<T> {
    val shouldBeDisposed: Boolean
}
