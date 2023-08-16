package paintbox.binding

import java.lang.ref.WeakReference

/**
 * A listener that invalidates its parent in [weakParentRef] when a var (from [onChange]) changes.
 * It does not keep a strong reference to its parent.
 */
internal class InvalListener<T>(parentVar: ReadOnlyVar<T>) : DisposableVarChangedListener<Any?> {

    private val weakParentRef: WeakReference<ReadOnlyVar<T>> = WeakReference(parentVar)

    /**
     * Set to true when the item in [weakParentRef] is no longer accessible (is null).
     */
    private var parentIsGone: Boolean = false

    override val shouldBeDisposed: Boolean
        get() = parentIsGone

    override fun onChange(v: ReadOnlyVar<Any?>) {
        val parent = weakParentRef.get()
        if (!shouldBeDisposed && parent != null) {
            parent.invalidate()
        } else {
            parentIsGone = true
        }
    }
}