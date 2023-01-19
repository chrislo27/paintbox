package paintbox.util.gdxutils

import com.badlogic.gdx.graphics.Color


fun Color.grey(rgb: Float, a: Float = 1f): Color = this.set(rgb, rgb, rgb, a)

fun Color.set(r: Int, g: Int, b: Int, a: Int = 255): Color {
    this.set(r / 255f, g / 255f, b / 255f, a / 255f)
    return this
}

fun Color.alpha(newAlpha: Float): Color {
    this.a = newAlpha
    return this
}

fun Color.rgbToGreyscale(): Color {
    val y = (this.r * 0.2126f + this.g * 0.7152f + this.b * 0.0722f).coerceIn(0f, 1f)
    this.r = y
    this.g = y
    this.b = y
    return this
}
