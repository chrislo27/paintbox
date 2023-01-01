package paintbox.util


inline fun <reified I> Iterable<*>.filterAndIsInstance(predicate: (I) -> Boolean): List<I> {
    val list = mutableListOf<I>()
    for (element in this) if (element is I && predicate(element)) list.add(element)
    return list
}
