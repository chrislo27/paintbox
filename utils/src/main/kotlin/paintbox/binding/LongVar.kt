package paintbox.binding

/**
 * The [Long] specialization of [ReadOnlyVar].
 *
 * Provides the [get] method which is a primitive-type long.
 */
interface ReadOnlyLongVar : ReadOnlyVar<Long> {
    
    companion object {
        /**
         * Returns a constant value [ReadOnlyLongVar]. The implementation used is memory optimized and doesn't
         * have dependencies like [LongVar] would.
         */
        fun const(value: Long): ReadOnlyLongVar = ReadOnlyConstLongVar(value)
    }

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyLongVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive `long` value.
     *
     * If using this [ReadOnlyLongVar] in a binding, use [Var.Context] to do dependency tracking,
     * and use the `long` specialization specific functions ([Var.Context.use]).
     */
    fun get(): Long

    @Deprecated("Use ReadOnlyLongVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Long {
        return get() // WILL BE BOXED!
    }
}

internal class ReadOnlyConstLongVar(private val value: Long) : ReadOnlyVarBase<Long>(), ReadOnlyLongVar {
    override fun get(): Long {
        return value
    }
}

/**
 * The [Long] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type long.
 */
class LongVar : ReadOnlyVarBase<Long>, ReadOnlyLongVar, Var<Long> {

    private var binding: LongBinding
    private var currentValue: Long = 0L
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet() // Cannot be generic since it can depend on any other Var

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    constructor(item: Long) {
        binding = LongBinding.Const
        currentValue = item
    }

    constructor(computation: Var.Context.() -> Long) {
        binding = LongBinding.Compute(computation)
    }
    
    constructor(eager: Boolean, computation: Var.Context.() -> Long) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Long, sideEffecting: Var.Context.(existing: Long) -> Long) {
        binding = LongBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = 0L
    }

    override fun set(item: Long) {
        val existingBinding = binding
        if (existingBinding is LongBinding.Const && currentValue == item) {
            return
        }
        reset()
        currentValue = item
        binding = LongBinding.Const
        notifyListeners()
    }

    override fun bind(computation: Var.Context.() -> Long) {
        reset()
        binding = LongBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Long, sideEffecting: Var.Context.(existing: Long) -> Long) {
        reset()
        binding = LongBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: Var.Context.(existing: Long) -> Long) {
        sideEffecting(get(), sideEffecting)
    }

    override fun asReadOnlyVar(): ReadOnlyLongVar = this

    /**
     * The implementation of [getOrCompute] but returns a long primitive.
     */
    override fun get(): Long {
        val result: Long = when (val binding = this.binding) {
            is LongBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                this.currentValue
            }
            is LongBinding.Compute -> {
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
            is LongBinding.SideEffecting -> {
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
    
    
    @Deprecated("Use LongVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Long {
        return get() // WILL BE BOXED!
    }

    override fun toString(): String {
        return get().toString()
    }

    /**
     * [Sets][set] this [LongVar] to be the negation of its value from [LongVar.get].
     *
     * Returns the new state.
     */
    fun negate(): Long {
        val newState = -this.get()
        this.set(newState)
        return newState
    }
    
    /**
     * Increments this value by 1 and returns the ORIGINAL value.
     */
    fun getAndIncrement(): Long {
        val old = this.get()
        this.set(old + 1)
        return old
    }

    /**
     * Increments this value by 1 and returns the NEW value.
     */
    fun incrementAndGet(): Long {
        val newValue = this.get() + 1
        this.set(newValue)
        return newValue
    }

    /**
     * Increments this value by [amount] and returns the ORIGINAL value.
     */
    fun getAndIncrementBy(amount: Long): Long {
        val old = this.get()
        this.set(old + amount)
        return old
    }

    /**
     * Increments this value by [amount] and returns the NEW value.
     */
    fun incrementAndGetBy(amount: Long): Long {
        val newValue = this.get() + amount
        this.set(newValue)
        return newValue
    }

    /**
     * Decrements this value by 1 and returns the ORIGINAL value.
     */
    fun getAndDecrement(): Long {
        val old = this.get()
        this.set(old - 1)
        return old
    }

    /**
     * Decrements this value by 1 and returns the NEW value.
     */
    fun decrementAndGet(): Long {
        val newValue = this.get() - 1
        this.set(newValue)
        return newValue
    }

    /**
     * Decrements this value by [amount] and returns the ORIGINAL value.
     */
    fun getAndDecrementBy(amount: Long): Long {
        val old = this.get()
        this.set(old - amount)
        return old
    }

    /**
     * Decrements this value by [amount] and returns the NEW value.
     */
    fun decrementAndGetBy(amount: Long): Long {
        val newValue = this.get() - amount
        this.set(newValue)
        return newValue
    }

    private sealed class LongBinding {
        /**
         * Represents a constant value. The value is actually stored in [LongVar.currentValue].
         */
        object Const : LongBinding()

        class Compute(val computation: Var.Context.() -> Long) : LongBinding()

        class SideEffecting(var item: Long, val sideEffectingComputation: Var.Context.(existing: Long) -> Long) : LongBinding()
    }
}

/**
 * [Sets][Var.set] this [Long] [Var] to be the negation of its value from [Var.getOrCompute].
 *
 * Returns the new state.
 */
fun Var<Long>.negate(): Long {
    val newState = -this.getOrCompute()
    this.set(newState)
    return newState
}

/**
 * Increments this value by 1 and returns the ORIGINAL value.
 */
fun Var<Long>.getAndIncrement(): Long {
    val old = this.getOrCompute()
    this.set(old + 1)
    return old
}

/**
 * Increments this value by 1 and returns the NEW value.
 */
fun Var<Long>.incrementAndGet(): Long {
    val newValue = this.getOrCompute() + 1
    this.set(newValue)
    return newValue
}

/**
 * Increments this value by [amount] and returns the ORIGINAL value.
 */
fun Var<Long>.getAndIncrementBy(amount: Long): Long {
    val old = this.getOrCompute()
    this.set(old + amount)
    return old
}

/**
 * Increments this value by [amount] and returns the NEW value.
 */
fun Var<Long>.incrementAndGetBy(amount: Long): Long {
    val newValue = this.getOrCompute() + amount
    this.set(newValue)
    return newValue
}

/**
 * Decrements this value by 1 and returns the ORIGINAL value.
 */
fun Var<Long>.getAndDecrement(): Long {
    val old = this.getOrCompute()
    this.set(old - 1)
    return old
}

/**
 * Decrements this value by 1 and returns the NEW value.
 */
fun Var<Long>.decrementAndGet(): Long {
    val newValue = this.getOrCompute() - 1
    this.set(newValue)
    return newValue
}

/**
 * Decrements this value by [amount] and returns the ORIGINAL value.
 */
fun Var<Long>.getAndDecrementBy(amount: Long): Long {
    val old = this.getOrCompute()
    this.set(old - amount)
    return old
}

/**
 * Decrements this value by [amount] and returns the NEW value.
 */
fun Var<Long>.decrementAndGetBy(amount: Long): Long {
    val newValue = this.getOrCompute() - amount
    this.set(newValue)
    return newValue
}