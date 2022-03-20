package paintbox.binding

/**
 * The [Float] specialization of [ReadOnlyVar].
 * 
 * Provides the [get] method which is a primitive-type float.
 */
sealed interface ReadOnlyFloatVar : ReadOnlyVar<Float> {

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyFloatVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive float value.
     *
     * If using this [ReadOnlyFloatVar] in a binding, use [Var.Context] to do dependency tracking,
     * and use the `float` specialization specific functions ([Var.Context.use]).
     */
    fun get(): Float

    @Deprecated("Use ReadOnlyFloatVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Float {
        return get() // WILL BE BOXED!
    }
}

/**
 * The [Float] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type float.
 */
class FloatVar : ReadOnlyFloatVar, Var<Float> {
    
    private var binding: FloatBinding
    private var invalidated: Boolean = true // Used for Compute and SideEffecting bindings
    private var currentValue: Float = 0f
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet() // Cannot be generic since it can depend on any other Var

    private var listeners: Set<VarChangedListener<Float>> = emptySet()

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

    private fun notifyListeners() {
        var anyNeedToBeDisposed = 0
        val ls = listeners
        ls.forEach {
            it.onChange(this)
            if (it is DisposableVarChangedListener<*> && it.shouldBeDisposed) {
                anyNeedToBeDisposed += 1
            }
        }
        if (anyNeedToBeDisposed > 0) {
            listeners = ls.filterNotTo(LinkedHashSet(ls.size - anyNeedToBeDisposed)) { it is DisposableVarChangedListener<*> && it.shouldBeDisposed }
        }
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

    override fun addListener(listener: VarChangedListener<Float>) {
        if (listener !in listeners) {
            listeners = listeners + listener
        }
    }

    override fun removeListener(listener: VarChangedListener<Float>) {
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