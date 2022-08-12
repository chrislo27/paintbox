package paintbox.util


open class IntRect(open val x: Int, open val y: Int, open val width: Int, open val height: Int)

data class MutIntRect(override var x: Int, override var y: Int, override var width: Int, override var height: Int) : IntRect(x, y, width, height)
