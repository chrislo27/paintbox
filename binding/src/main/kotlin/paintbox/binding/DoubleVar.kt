package paintbox.binding


/**
 * Returns a constant value [ReadOnlyDoubleVar]. This directly calls [ReadOnlyDoubleVar.Companion.const].
 * @see ReadOnlyDoubleVar.Companion.const
 */
fun Double.toConstVar(): ReadOnlyDoubleVar = ReadOnlyDoubleVar.const(this)

/**
 * The [Double] specialization of [ReadOnlyVar].
 *
 * Provides the [get] method which is a primitive-type double.
 */
interface ReadOnlyDoubleVar : SpecializedReadOnlyVar<Double>, ReadOnlyVar<Double> {

    companion object {

        /**
         * Returns a constant value [ReadOnlyDoubleVar]. The implementation used is memory optimized and doesn't
         * have dependencies like [DoubleVar] would.
         */
        fun const(value: Double): ReadOnlyDoubleVar = ReadOnlyConstDoubleVar(value)
    }

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyDoubleVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive double value.
     *
     * If using this [ReadOnlyDoubleVar] in a binding, use [VarContext] to do dependency tracking,
     * and use the `double` specialization specific functions ([VarContext.use]).
     */
    fun get(): Double

    @Deprecated(
        "Use ReadOnlyDoubleVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
    override fun getOrCompute(): Double {
        return get() // WILL BE BOXED!
    }
}

internal class ReadOnlyConstDoubleVar(private val value: Double) : ReadOnlyVarBase<Double>(), ReadOnlyDoubleVar {

    override fun get(): Double {
        return value
    }
}

/**
 * The [Double] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type double.
 */
class DoubleVar : ReadOnlyVarBase<Double>, SpecializedVar<Double>, ReadOnlyDoubleVar, Var<Double> {

    private var binding: DoubleBinding
    private var currentValue: Double = 0.0
    private var dependencies: Set<ReadOnlyVar<Any?>> =
        emptySet() // Cannot be generic since it can depend on any other Var

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    constructor(item: Double) {
        binding = DoubleBinding.Const
        currentValue = item
    }

    constructor(computation: ContextBinding<Double>) {
        binding = DoubleBinding.Compute(computation)
    }

    constructor(eager: Boolean, computation: ContextBinding<Double>) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Double, sideEffecting: ContextSideEffecting<Double>) {
        binding = DoubleBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = 0.0
    }

    override fun set(item: Double) {
        val existingBinding = binding
        if (existingBinding is DoubleBinding.Const && currentValue == item) {
            return
        }
        reset()
        currentValue = item
        binding = DoubleBinding.Const
        notifyListeners()
    }

    override fun bind(computation: ContextBinding<Double>) {
        reset()
        binding = DoubleBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Double, sideEffecting: ContextSideEffecting<Double>) {
        reset()
        binding = DoubleBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: ContextSideEffecting<Double>) {
        sideEffecting(get(), sideEffecting)
    }

    override fun asReadOnlyVar(): ReadOnlyDoubleVar = this

    /**
     * The implementation of [getOrCompute] but returns a double primitive.
     */
    override fun get(): Double {
        val result: Double = when (val binding = this.binding) {
            is DoubleBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                this.currentValue
            }

            is DoubleBinding.Compute -> {
                if (!invalidated) {
                    currentValue
                } else {
                    val ctx = DependencyTrackingVarContext()
                    val result = binding.computation(ctx)
                    val oldDependencies = dependencies
                    oldDependencies.forEach { it.removeListener(invalidationListener) }
                    dependencies = ctx.dependencies
                    dependencies.forEach { it.addListener(invalidationListener) }
                    currentValue = result
                    invalidated = false
                    result
                }
            }

            is DoubleBinding.SideEffecting -> {
                if (invalidated) {
                    val ctx = DependencyTrackingVarContext()
                    val result = binding.sideEffectingComputation(ctx, binding.item)
                    val oldDependencies = dependencies
                    oldDependencies.forEach { it.removeListener(invalidationListener) }
                    dependencies = ctx.dependencies
                    dependencies.forEach { it.addListener(invalidationListener) }
                    currentValue = result
                    invalidated = false
                    binding.item = result
                }
                binding.item
            }
        }
        return result
    }


    @Deprecated(
        "Use DoubleVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
    override fun getOrCompute(): Double {
        return get() // WILL BE BOXED!
    }

    override fun toString(): String {
        return get().toString()
    }

    private sealed class DoubleBinding {
        /**
         * Represents a constant value. The value is actually stored in [DoubleVar.currentValue].
         */
        data object Const : DoubleBinding()

        class Compute(val computation: ContextBinding<Double>) : DoubleBinding()

        class SideEffecting(
            var item: Double,
            val sideEffectingComputation: ContextSideEffecting<Double>
        ) : DoubleBinding()
    }
}