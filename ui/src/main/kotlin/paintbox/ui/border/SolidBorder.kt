package paintbox.ui.border

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import paintbox.PaintboxGame
import paintbox.binding.BooleanVar
import paintbox.binding.ContextBinding
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.ui.UIElement
import paintbox.ui.area.Insets
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect
import kotlin.math.max
import kotlin.math.roundToInt


class SolidBorder(initColor: Color) : Border {

    val color: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initColor))
    val roundedCorners: BooleanVar = BooleanVar(false)

    constructor() : this(Color.WHITE)

    constructor(binding: ContextBinding<Color>) : this() {
        color.bind(binding)
    }

    constructor(binding: ReadOnlyVar<Color>) : this() {
        color.bind(binding)
    }

    override fun renderBorder(originX: Float, originY: Float, batch: SpriteBatch, element: UIElement) {
        val insets = element.border.getOrCompute()
        if (insets == Insets.ZERO) return

        val borderZone = element.borderZone
        val width = borderZone.width.get()
        val height = borderZone.height.get()
        if (width <= 0f || height <= 0f) return

        val x = originX + borderZone.x.get()
        val y = originY - borderZone.y.get()
        val lastColor = batch.packedColor
        val thisColor = this.color.getOrCompute()
        val opacity = element.apparentOpacity.get()
        val tmpColor = ColorStack.getAndPush().set(thisColor)
        tmpColor.a *= opacity
        batch.color = tmpColor

        if (roundedCorners.get()) {
            val paintboxSpritesheet = PaintboxGame.gameInstance.staticAssets.paintboxSpritesheet
//            batch.fillRect(rectX + roundedRad, rectY - rectH + roundedRad, rectW - roundedRad * 2, rectH - roundedRad * 2)
//            batch.fillRect(rectX, rectY - rectH + roundedRad, (roundedRad).toFloat(), rectH - roundedRad * 2)
//            batch.fillRect(rectX + rectW - roundedRad, rectY - rectH + roundedRad, (roundedRad).toFloat(), rectH - roundedRad * 2)
//            batch.fillRect(rectX + roundedRad, rectY - rectH, rectW - roundedRad * 2, (roundedRad).toFloat())
//            batch.fillRect(rectX + roundedRad, rectY - roundedRad, rectW - roundedRad * 2, (roundedRad).toFloat())
//            val roundedCornersSet = roundedCorners
//            batch.draw(if (Corner.TOP_LEFT in roundedCornersSet) roundedRect else spritesheetFill,
//                    rectX, rectY - roundedRad, (roundedRad).toFloat(), (roundedRad).toFloat()) // TL
//            batch.draw(if (Corner.BOTTOM_LEFT in roundedCornersSet) roundedRect else spritesheetFill,
//                    rectX, rectY - rectH + roundedRad, (roundedRad).toFloat(), (-roundedRad).toFloat()) // BL
//            batch.draw(if (Corner.TOP_RIGHT in roundedCornersSet) roundedRect else spritesheetFill,
//                    rectX + rectW, rectY - roundedRad, (-roundedRad).toFloat(), (roundedRad).toFloat()) // TR
//            batch.draw(if (Corner.BOTTOM_RIGHT in roundedCornersSet) roundedRect else spritesheetFill,
//                    rectX + rectW, rectY - rectH + roundedRad, (-roundedRad).toFloat(), (-roundedRad).toFloat()) // BR

            val leftRightHeight = height - insets.bottom - insets.top
            batch.fillRect(x, y - height + insets.bottom, insets.left, leftRightHeight)
            batch.fillRect(x + width - insets.right, y - height + insets.bottom, insets.right, leftRightHeight)
            val topBottomWidth = width - insets.left - insets.right
            batch.fillRect(x + insets.left, y - height, topBottomWidth, insets.bottom)
            batch.fillRect(x + insets.left, y - insets.top, topBottomWidth, insets.top)
            var roundedRect: TextureRegion =
                paintboxSpritesheet.getRoundedCornerForRadius(max(insets.left, insets.top).roundToInt())
            batch.draw(roundedRect, x, y - insets.top, insets.left, insets.top) // TL
            roundedRect = paintboxSpritesheet.getRoundedCornerForRadius(max(insets.left, insets.bottom).roundToInt())
            batch.draw(roundedRect, x, y - height + insets.bottom, insets.left, -insets.bottom) // BL
            roundedRect = paintboxSpritesheet.getRoundedCornerForRadius(max(insets.right, insets.top).roundToInt())
            batch.draw(roundedRect, x + width, y - insets.top, -insets.right, insets.top) // TR
            roundedRect = paintboxSpritesheet.getRoundedCornerForRadius(max(insets.right, insets.bottom).roundToInt())
            batch.draw(roundedRect, x + width, y - height + insets.bottom, -insets.right, -insets.bottom) // BR
        } else {
            batch.fillRect(x, y - height, insets.left, height)
            batch.fillRect(x + width - insets.right, y - height, insets.right, height)
            val topBottomWidth = width - insets.left - insets.right
            batch.fillRect(x + insets.left, y - height, topBottomWidth, insets.bottom)
            batch.fillRect(x + insets.left, y - insets.top, topBottomWidth, insets.top)
        }

        ColorStack.pop()
        batch.packedColor = lastColor
    }

}