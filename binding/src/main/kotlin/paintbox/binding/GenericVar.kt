package paintbox.binding


/**
 * The default implementation of [Var].
 *
 * Specialized versions of [Var] exist for primitives, such as [IntVar], [FloatVar], etc.
 */
class GenericVar<T> : ReadOnlyVarBase<T>, Var<T> {

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    private var binding: GenericBinding<T>
    private var currentValue: T? = null
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet()
        set(newValue) {
            field.forEach { it.removeListener(invalidationListener) }
            field = newValue
            newValue.forEach { it.addListener(invalidationListener) }
        }


    constructor(item: T) {
        currentValue = item
        binding = GenericBinding.Const
    }

    constructor(computation: ContextBinding<T>) {
        binding = GenericBinding.Compute(computation)
    }

    constructor(eager: Boolean, computation: ContextBinding<T>) : this(computation) {
        if (eager) {
            getOrCompute()
        }
    }

    constructor(readOnlyVar: ReadOnlyVar<T>) {
        binding = GenericBinding.BindToVar(readOnlyVar)
        dependencies = setOf(readOnlyVar)
    }

    constructor(item: T, sideEffecting: ContextSideEffecting<T>) {
        binding = GenericBinding.SideEffecting(item, sideEffecting)
    }

    private fun resetState() {
        dependencies = emptySet()
        invalidated = true
        currentValue = null
        // Note: do NOT call notifyListeners here.
    }

    override fun set(item: T) {
        val existingBinding = binding
        if (existingBinding is GenericBinding.Const && currentValue == item) {
            return
        }
        resetState()
        currentValue = item
        binding = GenericBinding.Const
        notifyListeners()
    }

    override fun bind(computation: ContextBinding<T>) {
        resetState()
        binding = GenericBinding.Compute(computation)
        notifyListeners()
    }

    override fun bind(readOnlyVar: ReadOnlyVar<T>) {
        resetState()
        binding = GenericBinding.BindToVar(readOnlyVar)
        dependencies = setOf(readOnlyVar)
        notifyListeners()
    }

    override fun sideEffecting(item: T, sideEffecting: ContextSideEffecting<T>) {
        resetState()
        binding = GenericBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: ContextSideEffecting<T>) {
        sideEffecting(getOrCompute(), sideEffecting)
    }

    override fun getOrCompute(): T {
        return when (val binding = this.binding) {
            is GenericBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                @Suppress("UNCHECKED_CAST") (currentValue as T)
            }

            is GenericBinding.Compute -> {
                if (!invalidated) {
                    @Suppress("UNCHECKED_CAST") (currentValue as T)
                } else {
                    val ctx = DependencyTrackingVarContext()
                    val result = binding.computation(ctx)
                    dependencies = ctx.dependencies
                    currentValue = result
                    invalidated = false
                    result
                }
            }

            is GenericBinding.SideEffecting -> {
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

            is GenericBinding.BindToVar -> {
                if (!invalidated) {
                    @Suppress("UNCHECKED_CAST") (currentValue as T)
                } else {
                    val result = binding.readOnlyVar.getOrCompute()
                    currentValue = result
                    invalidated = false
                    result
                }
            }
        }
    }

    override fun toString(): String {
        return getOrCompute().toString()
    }

    private sealed class GenericBinding<out T> {
        
        data object Const : GenericBinding<Nothing>()

        class Compute<T>(val computation: ContextBinding<T>) : GenericBinding<T>()

        class SideEffecting<T>(var item: T, val sideEffectingComputation: ContextSideEffecting<T>) : GenericBinding<T>()
        
        class BindToVar<T>(val readOnlyVar: ReadOnlyVar<T>) : GenericBinding<T>()
    }
}