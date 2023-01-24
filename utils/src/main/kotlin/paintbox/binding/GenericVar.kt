package paintbox.binding


/**
 * The default implementation of [Var].
 */
class GenericVar<T> : ReadOnlyVarBase<T>, Var<T> {

    private var binding: GenericBinding<T>
    private var currentValue: T? = null
    private var dependencies: Set<ReadOnlyVar<Any?>> =
        emptySet() // Cannot be generic since it can depend on any other Var

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    @Suppress("UNCHECKED_CAST")
    constructor(item: T) {
        currentValue = item
        binding = GenericBinding.Const
    }

    constructor(computation: Var.Context.() -> T) {
        binding = GenericBinding.Compute(computation)
    }

    constructor(eager: Boolean, computation: Var.Context.() -> T) : this(computation) {
        if (eager) {
            getOrCompute()
        }
    }

    constructor(item: T, sideEffecting: Var.Context.(existing: T) -> T) {
        binding = GenericBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = null
        // Note: do NOT call notifyListeners here.
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(item: T) {
        val existingBinding = binding
        if (existingBinding is GenericBinding.Const && currentValue == item) {
            return
        }
        reset()
        currentValue = item
        binding = GenericBinding.Const
        notifyListeners()
    }

    override fun bind(computation: Var.Context.() -> T) {
        reset()
        binding = GenericBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: T, sideEffecting: Var.Context.(existing: T) -> T) {
        reset()
        binding = GenericBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: Var.Context.(existing: T) -> T) {
        sideEffecting(getOrCompute(), sideEffecting)
    }

    override fun getOrCompute(): T {
        return when (val binding = this.binding) {
            is GenericBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                @Suppress("UNCHECKED_CAST") (currentValue as T) // Cannot be currentValue!! since the actual type of T may be nullable
            }

            is GenericBinding.Compute -> {
                if (!invalidated) {
                    @Suppress("UNCHECKED_CAST") (currentValue as T)
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

            is GenericBinding.SideEffecting -> {
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
    }

    override fun toString(): String {
        return getOrCompute().toString()
    }

    private sealed class GenericBinding<out T> {
        object Const : GenericBinding<Nothing>()

        class Compute<T>(val computation: Var.Context.() -> T) : GenericBinding<T>()

        class SideEffecting<T>(var item: T, val sideEffectingComputation: Var.Context.(existing: T) -> T) :
            GenericBinding<T>()
    }
}