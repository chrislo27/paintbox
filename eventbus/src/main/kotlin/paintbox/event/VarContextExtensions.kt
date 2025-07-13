package paintbox.event

import paintbox.binding.Var
import paintbox.binding.VarContext


/**
 * Adds a dependency on the given event bus, using the event as the value.
 */
context(varContext: VarContext)
fun <R> EventBus<R>.use(initialValue: R): R {
    return this.use(initialValue) { it }
}

/**
 * Adds a dependency on the given event bus, mapping a received event using [mapping].
 */
context(varContext: VarContext)
fun <Evt, R> EventBus<Evt>.use(initialValue: R, mapping: (Evt) -> R): R {
    val eventBus = this
    val auxVar = Var(initialValue)
    eventBus.addListener(EventListener.oneTime { e, _ ->
        auxVar.set(mapping(e))
        false
    })
    return varContext.bindAndGet(auxVar)
}