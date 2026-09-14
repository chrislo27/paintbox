package paintbox.ui.border

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import paintbox.PaintboxGame
import paintbox.binding.BooleanVar
import paintbox.binding.ContextBinding
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.ui.UIElement
import paintbox.ui.area.Insets
import paintbox.util.gdxutils.fillRect
import kotlin.math.max
import kotlin.math.roundToInt


class SolidBorder(initColor: Color) : Border {

    val color: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initColor))
    val roundedOffCorners: BooleanVar = BooleanVar(false)
    
    @Deprecated("Renamed to roundedOffCorners for clarity", ReplaceWith("roundedOffCorners"))
    val roundedCorners: BooleanVar get() = roundedOffCorners

    constructor() : this(Color.WHITE)

    constructor(binding: ContextBinding<Color>) : this() {
        color.bind(binding)
    }

    constructor(binding: ReadOnlyVar<Color>) : this() {
        color.bind(binding)
    }

    override fun renderBorder(originX: Float, originY: Float, batch: Batch, element: UIElement) {
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
        batch.setColor(thisColor.r, thisColor.g, thisColor.b, thisColor.a * opacity)

        if (roundedOffCorners.get()) {
            val paintboxSpritesheet = PaintboxGame.gameInstance.staticAssets.paintboxSpritesheet
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

        batch.packedColor = lastColor
    }

}