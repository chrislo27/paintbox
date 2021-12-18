package paintbox.binding

import java.lang.ref.WeakReference

/**
 * The [Double] specialization of [ReadOnlyVar].
 * 
 * Provides the [get] method which is a primitive-type double.
 */
sealed interface ReadOnlyDoubleVar : ReadOnlyVar<Double> {

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyDoubleVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive double value.
     *
     * If using this [ReadOnlyDoubleVar] in a binding, use [Var.Context] to do dependency tracking,
     * and use the `double` specialization specific functions ([Var.Context.use]).
     */
    fun get(): Double

    @Deprecated("Use ReadOnlyDoubleVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Double {
        return get() // WILL BE BOXED!
    }
}

/**
 * The [Double] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type double.
 */
class DoubleVar : ReadOnlyDoubleVar, Var<Double> {
    
    private var binding: DoubleBinding
    private var invalidated: Boolean = true // Used for Compute and SideEffecting bindings
    private var currentValue: Double = 0.0
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet() // Cannot be generic since it can depend on any other Var

    private var listeners: Set<VarChangedListener<Double>> = emptySet()

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    constructor(item: Double) {
        binding = DoubleBinding.Const
        currentValue = item
    }

    constructor(computation: Var.Context.() -> Double) {
        binding = DoubleBinding.Compute(computation)
    }

    constructor(item: Double, sideEffecting: Var.Context.(existing: Double) -> Double) {
        binding = DoubleBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = 0.0
    }

    private fun notifyListeners() {
        var anyNeedToBeDisposed = false
        listeners.forEach {
            it.onChange(this)
            if (it is InvalListener && it.disposeMe) {
                anyNeedToBeDisposed = true
            }
        }
        if (anyNeedToBeDisposed) {
            listeners = listeners.filter { it is InvalListener && it.disposeMe }.toSet()
        }
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

    override fun bind(computation: Var.Context.() -> Double) {
        reset()
        binding = DoubleBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Double, sideEffecting: Var.Context.(existing: Double) -> Double) {
        reset()
        binding = DoubleBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffecting(sideEffecting: Var.Context.(existing: Double) -> Double) {
        sideEffecting(get(), sideEffecting)
    }

    /**
     * The implementation of [getOrCompute] but returns a double primitive.
     */
    override fun get(): Double {
        val result: Double = when (val binding = this.binding) {
            is DoubleBinding.Const -> this.currentValue
            is DoubleBinding.Compute -> {
                if (!invalidated) {
                    currentValue
                } else {
                    val oldCurrentValue = currentValue
                    val ctx = Var.Context()
                    val result = binding.computation(ctx)
                    val oldDependencies = dependencies
                    oldDependencies.forEach { it.removeListener(invalidationListener) }
                    dependencies = ctx.dependencies
                    dependencies.forEach { it.addListener(invalidationListener) }
                    currentValue = result
                    invalidated = false
                    if (oldCurrentValue != currentValue) {
                        notifyListeners()
                    }
                    result
                }
            }
            is DoubleBinding.SideEffecting -> {
                if (invalidated) {
                    val oldCurrentValue = currentValue
                    val ctx = Var.Context()
                    val result = binding.sideEffectingComputation(ctx, binding.item)
                    val oldDependencies = dependencies
                    oldDependencies.forEach { it.removeListener(invalidationListener) }
                    dependencies = ctx.dependencies
                    dependencies.forEach { it.addListener(invalidationListener) }
                    currentValue = result
                    invalidated = false
                    if (oldCurrentValue != currentValue) {
                        notifyListeners()
                    }
                    binding.item = result
                }
                binding.item
            }
        }
        return result
    }


    @Deprecated("Use DoubleVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Double {
        return get() // WILL BE BOXED!
    }

    override fun addListener(listener: VarChangedListener<Double>) {
        if (listener !in listeners) {
            listeners = listeners + listener
        }
    }

    override fun removeListener(listener: VarChangedListener<Double>) {
        if (listener in listeners) {
            listeners = listeners - listener
        }
    }

    override fun toString(): String {
        return get().toString()
    }

    /**
     * Cannot be inner for garbage collection reasons! We are avoiding an explicit strong reference to the parent Var
     */
    private class InvalListener(v: DoubleVar) : VarChangedListener<Any?> {
        val weakRef: WeakReference<DoubleVar> = WeakReference(v)
        var disposeMe: Boolean = false
        
        override fun onChange(v: ReadOnlyVar<Any?>) {
            val parent = weakRef.get()
            if (!disposeMe && parent != null) {
                if (!parent.invalidated) {
                    parent.invalidated = true
                    parent.notifyListeners()
                }
            } else {
                disposeMe = true
            }
        }
    }

    private sealed class DoubleBinding {
        /**
         * Represents a constant value. The value is actually stored in [DoubleVar.currentValue].
         */
        object Const : DoubleBinding()

        class Compute(val computation: Var.Context.() -> Double) : DoubleBinding()

        class SideEffecting(var item: Double, val sideEffectingComputation: Var.Context.(existing: Double) -> Double) : DoubleBinding()
    }
}