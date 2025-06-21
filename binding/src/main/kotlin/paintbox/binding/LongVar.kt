package paintbox.binding


/**
 * Returns a constant value [ReadOnlyLongVar]. This directly calls [ReadOnlyLongVar.Companion.const].
 * @see ReadOnlyLongVar.Companion.const
 */
fun Long.toConstVar(): ReadOnlyLongVar = ReadOnlyLongVar.const(this)

/**
 * The [Long] specialization of [ReadOnlyVar].
 *
 * Provides the [get] method which is a primitive-type long.
 */
interface ReadOnlyLongVar : SpecializedReadOnlyVar<Long>, ReadOnlyVar<Long> {

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
     * If using this [ReadOnlyLongVar] in a binding, use [VarContext] to do dependency tracking,
     * and use the `long` specialization specific functions ([VarContext.use]).
     */
    fun get(): Long

    @Deprecated(
        "Use ReadOnlyLongVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
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
class LongVar : ReadOnlyVarBase<Long>, SpecializedVar<Long>, ReadOnlyLongVar, Var<Long> {

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    private var binding: LongBinding
    private var currentValue: Long = 0L
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet()
        set(newValue) {
            field.forEach { it.removeListener(invalidationListener) }
            field = newValue
            newValue.forEach { it.addListener(invalidationListener) }
        }

    constructor(item: Long) {
        binding = LongBinding.Const
        currentValue = item
    }

    constructor(computation: ContextBinding<Long>) {
        binding = LongBinding.Compute(computation)
    }

    constructor(eager: Boolean, computation: ContextBinding<Long>) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Long, sideEffecting: ContextSideEffecting<Long>) {
        binding = LongBinding.SideEffecting(item, sideEffecting)
    }

    private fun resetState() {
        dependencies.forEach { it.removeListener(invalidationListener) }
        dependencies = emptySet()
        invalidated = true
        currentValue = 0L
    }

    override fun set(item: Long) {
        val existingBinding = binding
        if (existingBinding is LongBinding.Const && currentValue == item) {
            return
        }
        resetState()
        currentValue = item
        binding = LongBinding.Const
        notifyListeners()
    }

    override fun bind(computation: ContextBinding<Long>) {
        resetState()
        binding = LongBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Long, sideEffecting: ContextSideEffecting<Long>) {
        resetState()
        binding = LongBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: ContextSideEffecting<Long>) {
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
                    val ctx = DependencyTrackingVarContext()
                    val result = binding.computation(ctx)
                    dependencies = ctx.dependencies
                    currentValue = result
                    invalidated = false
                    result
                }
            }

            is LongBinding.SideEffecting -> {
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
        "Use LongVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
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
        data object Const : LongBinding()

        class Compute(val computation: ContextBinding<Long>) : LongBinding()

        class SideEffecting(var item: Long, val sideEffectingComputation: ContextSideEffecting<Long>) : LongBinding()
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