package paintbox.binding


/**
 * Returns a constant value [ReadOnlyVar]. The implementation used is memory optimized and doesn't
 * have dependencies like [GenericVar] would.
 */
fun <T> T.toConstVar(): ReadOnlyVar<T> = ReadOnlyConstVar(this)

/**
 * Used by [ReadOnlyVar.const] as an internal implementation.
 */
internal class ReadOnlyConstVar<T>(private val value: T) : ReadOnlyVarBase<T>() {

    override fun getOrCompute(): T {
        return value
    }
}
