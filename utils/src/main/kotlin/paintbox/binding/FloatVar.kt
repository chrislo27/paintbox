package paintbox.binding

/**
 * The [Float] specialization of [ReadOnlyVar].
 * 
 * Provides the [get] method which is a primitive-type float.
 */
interface ReadOnlyFloatVar : ReadOnlyVar<Float> {

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
     * If using this [ReadOnlyFloatVar] in a binding, use [Var.Context] to do dependency tracking,
     * and use the `float` specialization specific functions ([Var.Context.use]).
     */
    fun get(): Float
    
    /**
     * Adds a *strong* reference listener to this [ReadOnlyFloatVar]. It can be removed with [removeListener].
     */
    fun addListener(listener: FloatVarChangedListener) {
        this.addListener(listener as VarChangedListener<Float>)
    }

    fun addFloatListener(listener: FloatVarChangedListener) = addListener(listener)

    @Deprecated("Use ReadOnlyFloatVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
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
 * A specialized [VarChangedListener] that only listens to a [ReadOnlyFloatVar].
 */
fun interface FloatVarChangedListener : VarChangedListener<Float> {

    fun onChange(v: ReadOnlyFloatVar)

    override fun onChange(v: ReadOnlyVar<Float>) {
        if (v is ReadOnlyFloatVar) {
            onChange(v)
        }
    }
}

/**
 * The [Float] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type float.
 */
class FloatVar : ReadOnlyVarBase<Float>, ReadOnlyFloatVar, Var<Float> {
    
    private var binding: FloatBinding
    private var currentValue: Float = 0f
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet() // Cannot be generic since it can depend on any other Var

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    constructor(item: Float) {
        binding = FloatBinding.Const
        currentValue = item
    }

    constructor(computation: Var.Context.() -> Float) {
        binding = FloatBinding.Compute(computation)
    }
    
    constructor(eager: Boolean, computation: Var.Context.() -> Float) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Float, sideEffecting: Var.Context.(existing: Float) -> Float) {
        binding = FloatBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = 0f
    }

    override fun set(item: Float) {
        val existingBinding = binding
        if (existingBinding is FloatBinding.Const && currentValue == item) {
            return
        }
        reset()
        currentValue = item
        binding = FloatBinding.Const
        notifyListeners()
    }

    override fun bind(computation: Var.Context.() -> Float) {
        reset()
        binding = FloatBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Float, sideEffecting: Var.Context.(existing: Float) -> Float) {
        reset()
        binding = FloatBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: Var.Context.(existing: Float) -> Float) {
        sideEffecting(get(), sideEffecting)
    }

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
                    val ctx = Var.Context()
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
            is FloatBinding.SideEffecting -> {
                if (invalidated) {
                    val ctx = Var.Context()
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


    @Deprecated("Use FloatVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
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
        object Const : FloatBinding()

        class Compute(val computation: Var.Context.() -> Float) : FloatBinding()

        class SideEffecting(var item: Float, val sideEffectingComputation: Var.Context.(existing: Float) -> Float) : FloatBinding()
    }
}