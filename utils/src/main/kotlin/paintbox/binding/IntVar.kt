package paintbox.binding

/**
 * The [Int] specialization of [ReadOnlyVar].
 *
 * Provides the [get] method which is a primitive-type int.
 */
interface ReadOnlyIntVar : SpecializedReadOnlyVar<Int>, ReadOnlyVar<Int> {

    companion object {
        /**
         * Returns a constant value [ReadOnlyIntVar]. The implementation used is memory optimized and doesn't
         * have dependencies like [IntVar] would.
         */
        fun const(value: Int): ReadOnlyIntVar = ReadOnlyConstIntVar(value)
    }

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyIntVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive `int` value.
     *
     * If using this [ReadOnlyIntVar] in a binding, use [Var.Context] to do dependency tracking,
     * and use the `int` specialization specific functions ([Var.Context.use]).
     */
    fun get(): Int

    @Deprecated("Use ReadOnlyIntVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Int {
        return get() // WILL BE BOXED!
    }
}

internal class ReadOnlyConstIntVar(private val value: Int) : ReadOnlyVarBase<Int>(), ReadOnlyIntVar {
    override fun get(): Int {
        return value
    }
}

/**
 * The [Int] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type int.
 */
class IntVar : ReadOnlyVarBase<Int>, SpecializedVar<Int>, ReadOnlyIntVar, Var<Int> {

    private var binding: IntBinding
    private var currentValue: Int = 0
    private var dependencies: Set<ReadOnlyVar<Any?>> = emptySet() // Cannot be generic since it can depend on any other Var

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    constructor(item: Int) {
        binding = IntBinding.Const
        currentValue = item
    }

    constructor(computation: Var.Context.() -> Int) {
        binding = IntBinding.Compute(computation)
    }
    
    constructor(eager: Boolean, computation: Var.Context.() -> Int) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Int, sideEffecting: Var.Context.(existing: Int) -> Int) {
        binding = IntBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = 0
    }

    override fun set(item: Int) {
        val existingBinding = binding
        if (existingBinding is IntBinding.Const && currentValue == item) {
            return
        }
        reset()
        currentValue = item
        binding = IntBinding.Const
        notifyListeners()
    }

    override fun bind(computation: Var.Context.() -> Int) {
        reset()
        binding = IntBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Int, sideEffecting: Var.Context.(existing: Int) -> Int) {
        reset()
        binding = IntBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: Var.Context.(existing: Int) -> Int) {
        sideEffecting(get(), sideEffecting)
    }

    override fun asReadOnlyVar(): ReadOnlyIntVar = this

    /**
     * The implementation of [getOrCompute] but returns a int primitive.
     */
    override fun get(): Int {
        val result: Int = when (val binding = this.binding) {
            is IntBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                this.currentValue
            }
            is IntBinding.Compute -> {
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
            is IntBinding.SideEffecting -> {
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


    @Deprecated("Use IntVar.get() instead to avoid explicit boxing",
            replaceWith = ReplaceWith("this.get()"),
            level = DeprecationLevel.ERROR)
    override fun getOrCompute(): Int {
        return get() // WILL BE BOXED!
    }

    override fun toString(): String {
        return get().toString()
    }

    /**
     * [Sets][set] this [IntVar] to be the negation of its value from [IntVar.get].
     *
     * Returns the new state.
     */
    fun negate(): Int {
        val newState = -this.get()
        this.set(newState)
        return newState
    }

    /**
     * Increments this value by 1 and returns the ORIGINAL value.
     */
    fun getAndIncrement(): Int {
        val old = this.get()
        this.set(old + 1)
        return old
    }
    
    /**
     * Increments this value by 1 and returns the NEW value.
     */
    fun incrementAndGet(): Int {
        val newValue = this.get() + 1
        this.set(newValue)
        return newValue
    }

    /**
     * Increments this value by [amount] and returns the ORIGINAL value.
     */
    fun getAndIncrementBy(amount: Int): Int {
        val old = this.get()
        this.set(old + amount)
        return old
    }
    
    /**
     * Increments this value by [amount] and returns the NEW value.
     */
    fun incrementAndGetBy(amount: Int): Int {
        val newValue = this.get() + amount
        this.set(newValue)
        return newValue
    }

    /**
     * Decrements this value by 1 and returns the ORIGINAL value.
     */
    fun getAndDecrement(): Int {
        val old = this.get()
        this.set(old - 1)
        return old
    }
    
    /**
     * Decrements this value by 1 and returns the NEW value.
     */
    fun decrementAndGet(): Int {
        val newValue = this.get() - 1
        this.set(newValue)
        return newValue
    }

    /**
     * Decrements this value by [amount] and returns the ORIGINAL value.
     */
    fun getAndDecrementBy(amount: Int): Int {
        val old = this.get()
        this.set(old - amount)
        return old
    }
    
    /**
     * Decrements this value by [amount] and returns the NEW value.
     */
    fun decrementAndGetBy(amount: Int): Int {
        val newValue = this.get() - amount
        this.set(newValue)
        return newValue
    }

    private sealed class IntBinding {
        /**
         * Represents a constant value. The value is actually stored in [IntVar.currentValue].
         */
        object Const : IntBinding()

        class Compute(val computation: Var.Context.() -> Int) : IntBinding()

        class SideEffecting(var item: Int, val sideEffectingComputation: Var.Context.(existing: Int) -> Int) : IntBinding()
    }
}

/**
 * [Sets][Var.set] this [Int] [Var] to be the negation of its value from [Var.getOrCompute].
 *
 * Returns the new state.
 */
fun Var<Int>.negate(): Int {
    val newState = -this.getOrCompute()
    this.set(newState)
    return newState
}

/**
 * Increments this value by 1 and returns the ORIGINAL value.
 */
fun Var<Int>.getAndIncrement(): Int {
    val old = this.getOrCompute()
    this.set(old + 1)
    return old
}

/**
 * Increments this value by 1 and returns the NEW value.
 */
fun Var<Int>.incrementAndGet(): Int {
    val newValue = this.getOrCompute() + 1
    this.set(newValue)
    return newValue
}

/**
 * Increments this value by [amount] and returns the ORIGINAL value.
 */
fun Var<Int>.getAndIncrementBy(amount: Int): Int {
    val old = this.getOrCompute()
    this.set(old + amount)
    return old
}

/**
 * Increments this value by [amount] and returns the NEW value.
 */
fun Var<Int>.incrementAndGetBy(amount: Int): Int {
    val newValue = this.getOrCompute() + amount
    this.set(newValue)
    return newValue
}

/**
 * Decrements this value by 1 and returns the ORIGINAL value.
 */
fun Var<Int>.getAndDecrement(): Int {
    val old = this.getOrCompute()
    this.set(old - 1)
    return old
}

/**
 * Decrements this value by 1 and returns the NEW value.
 */
fun Var<Int>.decrementAndGet(): Int {
    val newValue = this.getOrCompute() - 1
    this.set(newValue)
    return newValue
}

/**
 * Decrements this value by [amount] and returns the ORIGINAL value.
 */
fun Var<Int>.getAndDecrementBy(amount: Int): Int {
    val old = this.getOrCompute()
    this.set(old - amount)
    return old
}

/**
 * Decrements this value by [amount] and returns the NEW value.
 */
fun Var<Int>.decrementAndGetBy(amount: Int): Int {
    val newValue = this.getOrCompute() - amount
    this.set(newValue)
    return newValue
}