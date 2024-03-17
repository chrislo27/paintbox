package paintbox.binding

import java.lang.Character.MIN_VALUE as nullChar

/**
 * The [Char] specialization of [ReadOnlyVar].
 *
 * Provides the [get] method which is a primitive-type char.
 */
interface ReadOnlyCharVar : SpecializedReadOnlyVar<Char>, ReadOnlyVar<Char> {

    companion object {

        /**
         * Returns a constant value [ReadOnlyCharVar]. The implementation used is memory optimized and doesn't
         * have dependencies like [CharVar] would.
         */
        fun const(value: Char): ReadOnlyCharVar = ReadOnlyConstCharVar(value)
    }

    /**
     * Gets (and computes if necessary) the value represented by this [ReadOnlyCharVar].
     * Unlike the [ReadOnlyVar.getOrCompute] function, this will always return a primitive char value.
     *
     * If using this [ReadOnlyCharVar] in a binding, use [VarContext] to do dependency tracking,
     * and use the `char` specialization specific functions ([VarContext.use]).
     */
    fun get(): Char

    @Deprecated(
        "Use ReadOnlyCharVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
    override fun getOrCompute(): Char {
        return get() // WILL BE BOXED!
    }
}

internal class ReadOnlyConstCharVar(private val value: Char) : ReadOnlyVarBase<Char>(), ReadOnlyCharVar {

    override fun get(): Char {
        return value
    }
}

/**
 * The [Char] specialization of [Var].
 *
 * Provides the [get] method which is a primitive-type char.
 */
class CharVar : ReadOnlyVarBase<Char>, SpecializedVar<Char>, ReadOnlyCharVar, Var<Char> {

    private var binding: CharBinding
    private var currentValue: Char = nullChar
    private var dependencies: Set<ReadOnlyVar<Any?>> =
        emptySet() // Cannot be generic since it can depend on any other Var

    /**
     * This is intentionally generic type Any? so further unchecked casts are avoided when it is used
     */
    private val invalidationListener: VarChangedListener<Any?> = InvalListener(this)

    constructor(item: Char) {
        binding = CharBinding.Const
        currentValue = item
    }

    constructor(computation: ContextBinding<Char>) {
        binding = CharBinding.Compute(computation)
    }

    constructor(eager: Boolean, computation: ContextBinding<Char>) : this(computation) {
        if (eager) {
            get()
        }
    }

    constructor(item: Char, sideEffecting: ContextSideEffecting<Char>) {
        binding = CharBinding.SideEffecting(item, sideEffecting)
    }

    private fun reset() {
        dependencies = emptySet()
        invalidated = true
        currentValue = nullChar
    }

    override fun set(item: Char) {
        val existingBinding = binding
        if (existingBinding is CharBinding.Const && currentValue == item) {
            return
        }
        reset()
        currentValue = item
        binding = CharBinding.Const
        notifyListeners()
    }

    override fun bind(computation: ContextBinding<Char>) {
        reset()
        binding = CharBinding.Compute(computation)
        notifyListeners()
    }

    override fun sideEffecting(item: Char, sideEffecting: ContextSideEffecting<Char>) {
        reset()
        binding = CharBinding.SideEffecting(item, sideEffecting)
        notifyListeners()
    }

    override fun sideEffectingAndRetain(sideEffecting: ContextSideEffecting<Char>) {
        sideEffecting(get(), sideEffecting)
    }

    override fun asReadOnlyVar(): ReadOnlyCharVar = this

    /**
     * The implementation of [getOrCompute] but returns a boolean primitive.
     */
    override fun get(): Char {
        val result: Char = when (val binding = this.binding) {
            is CharBinding.Const -> {
                if (invalidated) {
                    invalidated = false
                }
                this.currentValue
            }

            is CharBinding.Compute -> {
                if (!invalidated) {
                    currentValue
                } else {
                    val ctx = VarContext()
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

            is CharBinding.SideEffecting -> {
                if (invalidated) {
                    val ctx = VarContext()
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


    @Deprecated(
        "Use CharVar.get() instead to avoid explicit boxing",
        replaceWith = ReplaceWith("this.get()"),
        level = DeprecationLevel.ERROR
    )
    override fun getOrCompute(): Char {
        return get() // WILL BE BOXED!
    }

    override fun toString(): String {
        return get().toString()
    }

    private sealed class CharBinding {
        /**
         * Represents a constant value. The value is actually stored in [BooleanVar.currentValue].
         */
        data object Const : CharBinding()

        class Compute(val computation: ContextBinding<Char>) : CharBinding()

        class SideEffecting(var item: Char, val sideEffectingComputation: ContextSideEffecting<Char>) : CharBinding()
    }
}
