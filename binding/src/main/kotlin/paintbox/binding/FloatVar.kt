package paintbox.binding


/**
 * Returns a constant value [ReadOnlyFloatVar]. This directly calls [ReadOnlyFloatVar.Companion.const].
 * @see ReadOnlyFloatVar.Companion.const
 */
fun Float.toConstVar(): ReadOnlyFloatVar = ReadOnlyFloatVar.const(this)

/**
 * The [Float] specialization of [ReadOnlyVar].
 *
 * Provides the [get] method which is a primitive-type float.
 */
interface ReadOnlyFloatVar : SpecializedReadOnlyVar<Float>, ReadOnlyVar<Float> {

    companion object {

        /**
         * Returns a constant value [ReadOnlyFloatVar]. The implementation used is memory optimized and doesn't
         * have dependencies like [FloatVar] would.
         */
        fun const(value: Float): ReadOnlyFloatVar = ReadOnlyConstFloatVar(value)
    }

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyFloatVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive float value.
     *
     * If using this [ReadOnlyFloatVar] in a binding, use [VarContext] to do dependency tracking,
     * and use the `float` specialization specific functions ([VarContext.use]).
     */
    fun get(): Float

    @Deprecated(
        "Use ReadOnlyFloatVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
    override fun getOrCompute(): Float {
        return get() // WILL BE BOXED!
    }
}

internal class ReadOnlyConstFloatVar(private val value: Float) : ReadOnlyVarBase<Float>(), ReadOnlyFloatVar {

    override fun get(): Float {
        return value
    }
}

/**
 * The [Float] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type float.
 */
class FloatVar : ReadOnlyVarBase<Float>, SpecializedVar<Float>, ReadOnlyFloatVar, Var<Float> {

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    private var binding: FloatBinding
    private var currentValue: Float = 0f
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet()
        set(newValue) {
            field.forEach { it.removeListener(invalidationListener) }
            field = newValue
            newValue.forEach { it.addListener(invalidationListener) }
        }

    constructor(item: Float) {
        binding = FloatBinding.Const
        currentValue = item
    }

    constructor(computation: ContextBinding<Float>) {
        binding = FloatBinding.Compute(computation)
    }

    constructor(eager: Boolean, computation: ContextBinding<Float>) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Float, sideEffecting: ContextSideEffecting<Float>) {
        binding = FloatBinding.SideEffecting(item, sideEffecting)
    }

    private fun resetState() {
        dependencies.forEach { it.removeListener(invalidationListener) }
        dependencies = emptySet()
        invalidated = true
        currentValue = 0f
    }

    override fun set(item: Float) {
        val existingBinding = binding
        if (existingBinding is FloatBinding.Const && currentValue == item) {
            return
        }
        resetState()
        currentValue = item
        binding = FloatBinding.Const
        notifyListeners()
    }

    override fun bind(computation: ContextBinding<Float>) {
        resetState()
        binding = FloatBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Float, sideEffecting: ContextSideEffecting<Float>) {
        resetState()
        binding = FloatBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: ContextSideEffecting<Float>) {
        sideEffecting(get(), sideEffecting)
    }

    override fun asReadOnlyVar(): ReadOnlyFloatVar = this

    /**
     * The implementation of [getOrCompute] but returns a float primitive.
     */
    override fun get(): Float {
        val result: Float = when (val binding = this.binding) {
            is FloatBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                this.currentValue
            }

            is FloatBinding.Compute -> {
                if (!invalidated) {
                    currentValue
                } else {
                    val ctx = DependencyTrackingVarContext()
                    val result = binding.computation(ctx)
                    dependencies = ctx.dependencies
                    currentValue = result
                    invalidated = false
                    result
                }
            }

            is FloatBinding.SideEffecting -> {
                if (invalidated) {
                    val ctx = DependencyTrackingVarContext()
                    val result = binding.sideEffectingComputation(ctx, binding.item)
                    dependencies = ctx.dependencies
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
        "Use FloatVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
    override fun getOrCompute(): Float {
        return get() // WILL BE BOXED!
    }

    override fun toString(): String {
        return get().toString()
    }

    private sealed class FloatBinding {
        
        /**
         * Represents a constant value. The value is actually stored in [FloatVar.currentValue].
         */
        data object Const : FloatBinding()

        class Compute(val computation: ContextBinding<Float>) : FloatBinding()

        class SideEffecting(var item: Float, val sideEffectingComputation: ContextSideEffecting<Float>) : FloatBinding()
    }
}