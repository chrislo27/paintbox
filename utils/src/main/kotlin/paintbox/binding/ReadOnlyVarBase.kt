package paintbox.binding


/**
 * Returns a constant value [ReadOnlyVar]. The implementation used is memory optimized and doesn't
 * have dependencies like [GenericVar] would.
 */
fun <T> T.asReadOnlyVar(): ReadOnlyVar<T> = ReadOnlyConstVar(this)

/**
 * Returns a constant value [ReadOnlyBooleanVar]. This directly calls [ReadOnlyBooleanVar.Companion.const].
 * @see ReadOnlyBooleanVar.Companion.const
 */
fun Boolean.asReadOnlyVar(): ReadOnlyBooleanVar = ReadOnlyBooleanVar.const(this)

/**
 * Returns a constant value [ReadOnlyDoubleVar]. This directly calls [ReadOnlyDoubleVar.Companion.const].
 * @see ReadOnlyDoubleVar.Companion.const
 */
fun Double.asReadOnlyVar(): ReadOnlyDoubleVar = ReadOnlyDoubleVar.const(this)

/**
 * Returns a constant value [ReadOnlyFloatVar]. This directly calls [ReadOnlyFloatVar.Companion.const].
 * @see ReadOnlyFloatVar.Companion.const
 */
fun Float.asReadOnlyVar(): ReadOnlyFloatVar = ReadOnlyFloatVar.const(this)

/**
 * Returns a constant value [ReadOnlyIntVar]. This directly calls [ReadOnlyIntVar.Companion.const].
 * @see ReadOnlyIntVar.Companion.const
 */
fun Int.asReadOnlyVar(): ReadOnlyIntVar = ReadOnlyIntVar.const(this)

/**
 * Returns a constant value [ReadOnlyLongVar]. This directly calls [ReadOnlyLongVar.Companion.const].
 * @see ReadOnlyLongVar.Companion.const
 */
fun Long.asReadOnlyVar(): ReadOnlyLongVar = ReadOnlyLongVar.const(this)


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

/**
 * Used by [ReadOnlyVar.const] as an internal implementation.
 */
internal class ReadOnlyConstVar<T>(private val value: T) : ReadOnlyVarBase<T>() {
    override fun getOrCompute(): T {
        return value
    }
}
