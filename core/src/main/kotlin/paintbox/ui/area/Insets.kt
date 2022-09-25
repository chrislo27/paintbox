package paintbox.ui.area

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


data class Insets(val top: Float, val bottom: Float, val left: Float, val right: Float) {
    companion object {
        val ZERO: Insets = Insets(0f, 0f, 0f, 0f)

        private val intCache: Array<Insets> by lazy {
            Array(1 + 20) { i ->
                if (i == 0) {
                    ZERO
                } else {
                    val f = i.toFloat()
                    Insets(f, f, f, f)
                }
            }
        }
        
        private fun getCachedOrNew(all: Float): Insets {
            val toInt = all.toInt()
            if (floor(all) == all && toInt in intCache.indices) {
                return intCache[toInt]
            }
            return Insets(all, all, all, all)
        }

        operator fun invoke(all: Float): Insets {
            return getCachedOrNew(all)
        }
        
        operator fun invoke(topAndBottom: Float, leftAndRight: Float): Insets {
            if (topAndBottom == leftAndRight) {
                return getCachedOrNew(topAndBottom)
            }
            return Insets(topAndBottom, topAndBottom, leftAndRight, leftAndRight)
        }
    }
    
    
    fun maximize(other: Insets): Insets =
            Insets(max(this.top, other.top), max(this.bottom, other.bottom), max(this.left, other.left), max(this.right, other.right))
    fun minimize(other: Insets): Insets = 
            Insets(min(this.top, other.top), min(this.bottom, other.bottom), min(this.left, other.left), min(this.right, other.right))
    
    operator fun plus(other: Insets): Insets = Insets(this.top + other.top, this.bottom + other.bottom, this.left + other.left, this.right + other.right)
    operator fun minus(other: Insets): Insets = Insets(this.top - other.top, this.bottom - other.bottom, this.left - other.left, this.right - other.right)
    operator fun times(other: Insets): Insets = Insets(this.top * other.top, this.bottom * other.bottom, this.left * other.left, this.right * other.right)
    operator fun times(multiplier: Float): Insets = Insets(this.top * multiplier, this.bottom * multiplier, this.left * multiplier, this.right * multiplier)
    
    fun leftAndRight(): Float = this.left + this.right
    fun topAndBottom(): Float = this.top + this.bottom
}
