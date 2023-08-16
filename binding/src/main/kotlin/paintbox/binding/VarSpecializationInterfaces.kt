package paintbox.binding


/**
 * Marker interface that this [ReadOnlyVar] is primitive-specialized.
 *
 * Note that there are no abstract functions in this interface, precisely to avoid boxing!
 * Implementors should have a `get` function to get the value of this [ReadOnlyVar] as a primitive.
 */
interface SpecializedReadOnlyVar<T> : ReadOnlyVar<T>

/**
 * Marker interface that this mutable [Var] is primitive-specialized.
 *
 * Note that there are no abstract functions in this interface, precisely to avoid boxing!
 */
interface SpecializedVar<T> : Var<T>, SpecializedReadOnlyVar<T>
