package paintbox.ui2

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar

interface ReadOnlyBounds {

    val x: ReadOnlyFloatVar
    val y: ReadOnlyFloatVar
    val width: ReadOnlyFloatVar
    val height: ReadOnlyFloatVar

    /**
     * Returns true if the x/y point is within this bounds locally.
     */
    fun containsPointLocal(x: Float, y: Float): Boolean {
        val thisX = this.x.get()
        val thisY = this.y.get()
        val width = this.width.get()
        val height = this.height.get()
        return x >= thisX && x <= thisX + width && y >= thisY && y <= thisY + height
    }
}

interface Bounds : ReadOnlyBounds {

    override val x: FloatVar
    override val y: FloatVar
    override val width: FloatVar
    override val height: FloatVar

}

class BoundsImpl(
    override val x: FloatVar = FloatVar(0f),
    override val y: FloatVar = FloatVar(0f),
    override val width: FloatVar = FloatVar(0f),
    override val height: FloatVar = FloatVar(0f),
) : Bounds
