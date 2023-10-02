package paintbox.util

inline fun <T> Iterable<T>.sumOfFloat(selector: (T) -> Float): Float {
    var sum: Float = 0.0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

inline fun <reified I> Iterable<*>.filterAndIsInstance(predicate: (I) -> Boolean): List<I> {
    val list = mutableListOf<I>()
    for (element in this) if (element is I && predicate(element)) list.add(element)
    return list
}
