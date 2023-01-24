package paintbox.util


class ListOfOnes(override val size: Int) : List<Int>, RandomAccess {

    private inner class Iter(val startIndex: Int = 0) : ListIterator<Int> {

        private var index: Int = startIndex
        override fun hasNext(): Boolean = index < size
        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            index++
            return 1
        }

        override fun nextIndex(): Int = index
        override fun hasPrevious(): Boolean = index > 0
        override fun previousIndex(): Int = index - 1
        override fun previous(): Int {
            if (!hasPrevious()) throw NoSuchElementException()
            index--
            return 1
        }
    }

    init {
        require(size >= 0) { "Size must be non-negative (got $size)" }
    }

    override fun equals(other: Any?): Boolean = other is List<*> && other.size == this.size && other.all { it == 1 }
    override fun hashCode(): Int = 1 + size
    override fun toString(): String = buildString {
        append('[')
        for (i in 0 until size) {
            append('1')
            if (i < size - 1) {
                append(", ")
            }
        }
        append(']')
    }

    override fun isEmpty(): Boolean = size == 0
    override fun contains(element: Int): Boolean = isNotEmpty() && element == 1
    override fun containsAll(elements: Collection<Int>): Boolean = isNotEmpty() && elements.all { it == 1 }

    override fun get(index: Int): Int {
        if (index !in 0 until size) {
            throw IndexOutOfBoundsException("Index out of bounds, got $index, should be in [0, $size)")
        }
        return 1
    }

    override fun indexOf(element: Int): Int = if (isEmpty() || element != 1) -1 else 0
    override fun lastIndexOf(element: Int): Int = if (isEmpty() || element != 1) (size - 1) else 0

    override fun iterator(): Iterator<Int> = this.Iter()
    override fun listIterator(): ListIterator<Int> = this.Iter()
    override fun listIterator(index: Int): ListIterator<Int> {
        if (index !in 0 until size) throw IndexOutOfBoundsException("Index: $index")
        return this.Iter(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<Int> {
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex = $fromIndex")
        if (toIndex > size) throw IndexOutOfBoundsException("toIndex = $toIndex")
        require(fromIndex <= toIndex) { "fromIndex($fromIndex) > toIndex($toIndex)" }
        return ListOfOnes(toIndex - fromIndex + 1)
    }
}