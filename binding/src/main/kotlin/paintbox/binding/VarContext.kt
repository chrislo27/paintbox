package paintbox.binding

import paintbox.event.EventBus
import paintbox.event.EventListener


/**
 * Contextualizes [ReadOnlyVar] usage. This can be used for dependency tracking during binding (its main purpose),
 * reporting of used vars, or other logic that would want to intercept the reading/binding of a var.
 *
 * There are specialized functions for [SpecializedReadOnlyVar] to avoid boxing primitive values.
 */
interface VarContext {

    /**
     * Adds the [varr] as a dependency and returns the var's value.
     *
     * This function can be used on any [ReadOnlyVar] (even non-specialized ones) for compatibility. It is
     * recommended to use the receiver-style `use` function where possible.
     *
     * Note that if the receiver is a primitive-specialized var,
     * the appropriate specialized `use` function should be used instead.
     */
    fun <R> bindAndGet(varr: ReadOnlyVar<R>): R // NB: DON'T add specialized deprecations for this non-receiver-type function

    /**
     * Adds the receiver [var][R] as a dependency and returns the var's value.
     *
     * This is the "receiver-style" syntax for more fluent usage.
     * Note that if the receiver is a primitive-specialized var,
     * the appropriate specialized `use` function should be used instead.
     */
    fun <R> ReadOnlyVar<R>.use(): R { // Specializations should be added for this receiver-type function
        return bindAndGet(this)
    }

    /**
     * Adds a dependency on the given event bus, using the event as the value.
     */
    fun <R> EventBus<R>.use(initialValue: R): R {
        return this.use(initialValue) { it }
    }

    /**
     * Adds a dependency on the given event bus, mapping a received event using [mapping].
     */
    fun <Evt, R> EventBus<Evt>.use(initialValue: R, mapping: (Evt) -> R): R {
        val eventBus = this
        val auxVar = Var(initialValue)
        eventBus.addListener(EventListener.oneTime { e, _ ->
            auxVar.set(mapping(e))
            false
        })
        return this@VarContext.bindAndGet(auxVar)
    }


    //region Specialization methods

    @Deprecated(
        "Use the specialized `use` method for this SpecializedReadOnlyVar, or use the `bindAndGet` method",
        replaceWith = ReplaceWith("bindAndGet(this)"),
        level = DeprecationLevel.ERROR
    )
    fun <T> SpecializedReadOnlyVar<T>.use(): T {
        return bindAndGet(this)
    }


    @Deprecated(
        "If this is a ReadOnlyFloatVar, use ReadOnlyFloatVar.use() instead to avoid explicit boxing, or use the `bindAndGet` method",
        replaceWith = ReplaceWith("(this as ReadOnlyFloatVar).use()"),
        level = DeprecationLevel.ERROR
    )
    fun ReadOnlyVar<Float>.use(): Float {
        return bindAndGet(this)
    }

    /**
     * The float specialization method. Adds the receiver as a dependency and returns a primitive float.
     */
    fun ReadOnlyFloatVar.use(): Float


    @Deprecated(
        "If this is a ReadOnlyBooleanVar, use ReadOnlyBooleanVar.use() instead to avoid explicit boxing, or use the `bindAndGet` method",
        replaceWith = ReplaceWith("(this as ReadOnlyBooleanVar).use()"),
        level = DeprecationLevel.WARNING
    )
    fun ReadOnlyVar<Boolean>.use(): Boolean {
        return bindAndGet(this)
    }

    /**
     * The boolean specialization method. Adds the receiver as a dependency and returns a primitive boolean.
     */
    fun ReadOnlyBooleanVar.use(): Boolean


    @Deprecated(
        "If this is aReadOnlyIntVar, use ReadOnlyIntVar.use() instead to avoid explicit boxing, or use the `bindAndGet` method",
        replaceWith = ReplaceWith("(this as ReadOnlyIntVar).use()"),
        level = DeprecationLevel.ERROR
    )
    fun ReadOnlyVar<Int>.use(): Int {
        return bindAndGet(this)
    }

    /**
     * The int specialization method. Adds the receiver as a dependency and returns a primitive int.
     */
    fun ReadOnlyIntVar.use(): Int


    @Deprecated(
        "If this is a ReadOnlyLongVar, use ReadOnlyLongVar.use() instead to avoid explicit boxing, or use the `bindAndGet` method",
        replaceWith = ReplaceWith("(this as ReadOnlyLongVar).use()"),
        level = DeprecationLevel.ERROR
    )
    fun ReadOnlyVar<Long>.use(): Long {
        return bindAndGet(this)
    }

    /**
     * The long specialization method. Adds the receiver as a dependency and returns a primitive long.
     */
    fun ReadOnlyLongVar.use(): Long


    @Deprecated(
        "If this is a ReadOnlyDoubleVar, use ReadOnlyDoubleVar.use() instead to avoid explicit boxing, or use the `bindAndGet` method",
        replaceWith = ReplaceWith("(this as ReadOnlyDoubleVar).use()"),
        level = DeprecationLevel.ERROR
    )
    fun ReadOnlyVar<Double>.use(): Double {
        return bindAndGet(this)
    }

    /**
     * The double specialization method. Adds the receiver as a dependency and returns a primitive double.
     */
    fun ReadOnlyDoubleVar.use(): Double


    @Deprecated(
        "If this is a ReadOnlyCharVar, use ReadOnlyCharVar.use() instead to avoid explicit boxing, or use the `bindAndGet` method",
        replaceWith = ReplaceWith("(this as ReadOnlyCharVar).use()"),
        level = DeprecationLevel.ERROR
    )
    fun ReadOnlyVar<Char>.use(): Char {
        return bindAndGet(this)
    }

    /**
     * The char specialization method. Adds the receiver as a dependency and returns a primitive char.
     */
    fun ReadOnlyCharVar.use(): Char

    //endregion
}