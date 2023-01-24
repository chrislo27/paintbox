package paintbox.binding


/**
 * The [Boolean] specialization of [ReadOnlyVar].
 *
 * Provides the [get] method which is a primitive-type boolean.
 */
interface ReadOnlyBooleanVar : ReadOnlyVar<Boolean> {

    companion object {
        /**
         * Returns a constant value [ReadOnlyBooleanVar]. The implementation used is memory optimized and doesn't
         * have dependencies like [BooleanVar] would.
         */
        fun const(value: Boolean): ReadOnlyBooleanVar = ReadOnlyConstBooleanVar(value)
    }

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyBooleanVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive boolean value.
     *
     * If using this [ReadOnlyBooleanVar] in a binding, use [Var.Context] to do dependency tracking,
     * and use the `boolean` specialization specific functions ([Var.Context.use]).
     */
    fun get(): Boolean

    @Deprecated("Use ReadOnlyBooleanVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Boolean {
        return get() // WILL BE BOXED!
    }
}

internal class ReadOnlyConstBooleanVar(private val value: Boolean) : ReadOnlyVarBase<Boolean>(), ReadOnlyBooleanVar {
    override fun get(): Boolean {
        return value
    }
}

/**
 * The [Boolean] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type boolean.
 */
class BooleanVar : ReadOnlyVarBase<Boolean>, ReadOnlyBooleanVar, Var<Boolean> {

    private var binding: BooleanBinding
    private var currentValue: Boolean = false
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet() // Cannot be generic since it can depend on any other Var

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    constructor(item: Boolean) {
        binding = BooleanBinding.Const
        currentValue = item
    }

    constructor(computation: Var.Context.() -> Boolean) {
        binding = BooleanBinding.Compute(computation)
    }

    constructor(eager: Boolean, computation: Var.Context.() -> Boolean) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Boolean, sideEffecting: Var.Context.(existing: Boolean) -> Boolean) {
        binding = BooleanBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = false
    }

    override fun set(item: Boolean) {
        val existingBinding = binding
        if (existingBinding is BooleanBinding.Const && currentValue == item) {
            return
        }
        reset()
        currentValue = item
        binding = BooleanBinding.Const
        notifyListeners()
    }

    override fun bind(computation: Var.Context.() -> Boolean) {
        reset()
        binding = BooleanBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Boolean, sideEffecting: Var.Context.(existing: Boolean) -> Boolean) {
        reset()
        binding = BooleanBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: Var.Context.(existing: Boolean) -> Boolean) {
        sideEffecting(get(), sideEffecting)
    }

    override fun asReadOnlyVar(): ReadOnlyBooleanVar = this

    /**
     * The implementation of [getOrCompute] but returns a boolean primitive.
     */
    override fun get(): Boolean {
        val result: Boolean = when (val binding = this.binding) {
            is BooleanBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                this.currentValue
            }
            is BooleanBinding.Compute -> {
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
            is BooleanBinding.SideEffecting -> {
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


    @Deprecated("Use BooleanVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Boolean {
        return get() // WILL BE BOXED!
    }

    override fun toString(): String {
        return get().toString()
    }

    /**
     * [Sets][set] this [BooleanVar] to be the negation of its value from [BooleanVar.get].
     *
     * Returns the new state.
     */
    fun invert(): Boolean {
        val newState = !this.get()
        this.set(newState)
        return newState
    }

    private sealed class BooleanBinding {
        /**
         * Represents a constant value. The value is actually stored in [BooleanVar.currentValue].
         */
        object Const : BooleanBinding()

        class Compute(val computation: Var.Context.() -> Boolean) : BooleanBinding()

        class SideEffecting(var item: Boolean, val sideEffectingComputation: Var.Context.(existing: Boolean) -> Boolean) : BooleanBinding()
    }
}

/**
 * [Sets][Var.set] this [Boolean] [Var] to be the negation of its value from [Var.getOrCompute].
 *
 * Returns the new state.
 */
fun Var<Boolean>.invert(): Boolean {
    val newState = !this.getOrCompute()
    this.set(newState)
    return newState
}
