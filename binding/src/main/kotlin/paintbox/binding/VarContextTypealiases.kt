package paintbox.binding


typealias ContextBinding<R> = VarContext.() -> R

typealias ContextSideEffecting<R> = VarContext.(existing: R) -> R
