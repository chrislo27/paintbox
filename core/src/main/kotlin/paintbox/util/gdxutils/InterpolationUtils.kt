package paintbox.util.gdxutils

import com.badlogic.gdx.math.Interpolation


private class BoomerangInterpolation(
    private val original: Interpolation,
    private val midpoint: Float = 0.5f,
) : Interpolation() {

    override fun apply(a: Float): Float {
        if (a <= midpoint) {
            return original.apply(a / midpoint)
        }

        val inverseMidpoint = 1f - midpoint
        return original.apply(1f - (a - midpoint) / inverseMidpoint)
    }
}

fun Interpolation.toBoomerang(): Interpolation = BoomerangInterpolation(this)

fun Interpolation.toBoomerang(midpoint: Float): Interpolation {
    if (midpoint <= 0f || midpoint >= 1f) {
        throw IllegalArgumentException("Midpoint must be in open range (0, 1); got $midpoint")
    }
    return BoomerangInterpolation(this, midpoint)
}
