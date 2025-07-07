package paintbox.ui.element

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import paintbox.PaintboxGame
import paintbox.binding.ContextBinding
import paintbox.binding.IntVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.ui.Corner
import paintbox.ui.UIElement
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect
import java.util.*


open class RoundedRectElement(initColor: Color) : UIElement() {

    val color: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initColor))
    val roundedRadius: IntVar = IntVar(4)
    val roundedCorners: Var<EnumSet<Corner>> = Var(EnumSet.allOf(Corner::class.java))

    constructor() : this(Color.WHITE)

    constructor(binding: ContextBinding<Color>) : this() {
        color.bind(binding)
    }

    constructor(bindable: ReadOnlyVar<Color>) : this() {
        color.bind(bindable)
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val renderBounds = this.paddingZone
        val rectX = renderBounds.x.get() + originX
        val rectY = originY - renderBounds.y.get()
        val rectW = renderBounds.width.get()
        val rectH = renderBounds.height.get()
        val lastPackedColor = batch.packedColor

        val opacity: Float = this.apparentOpacity.get()
        val tmpColor: Color = ColorStack.getAndPush()
        tmpColor.set(color.getOrCompute())
        tmpColor.a *= opacity
        batch.color = tmpColor
        
        var roundedRad = roundedRadius.get()
        val paintboxSpritesheet = PaintboxGame.gameInstance.staticAssets.paintboxSpritesheet
        val spritesheetFill: TextureRegion = paintboxSpritesheet.fill
        if (roundedRad > rectW / 2f) {
            roundedRad = (rectW / 2f).toInt()
        }
        if (roundedRad > rectH / 2f) {
            roundedRad = (rectH / 2f).toInt()
        }
        if (roundedRad <= 0) {
            batch.fillRect(rectX, rectY - rectH, rectW, rectH)
        } else {
            val roundedRect: TextureRegion = paintboxSpritesheet.getRoundedCornerForRadius(roundedRad)
            batch.fillRect(
                rectX + roundedRad,
                rectY - rectH + roundedRad,
                rectW - roundedRad * 2,
                rectH - roundedRad * 2
            )
            batch.fillRect(rectX, rectY - rectH + roundedRad, (roundedRad).toFloat(), rectH - roundedRad * 2)
            batch.fillRect(
                rectX + rectW - roundedRad,
                rectY - rectH + roundedRad,
                (roundedRad).toFloat(),
                rectH - roundedRad * 2
            )
            batch.fillRect(rectX + roundedRad, rectY - rectH, rectW - roundedRad * 2, (roundedRad).toFloat())
            batch.fillRect(rectX + roundedRad, rectY - roundedRad, rectW - roundedRad * 2, (roundedRad).toFloat())
            val roundedCornersSet = roundedCorners.getOrCompute()
            batch.draw(
                if (Corner.TOP_LEFT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX, rectY - roundedRad, (roundedRad).toFloat(), (roundedRad).toFloat()
            ) // TL
            batch.draw(
                if (Corner.BOTTOM_LEFT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX, rectY - rectH + roundedRad, (roundedRad).toFloat(), (-roundedRad).toFloat()
            ) // BL
            batch.draw(
                if (Corner.TOP_RIGHT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX + rectW, rectY - roundedRad, (-roundedRad).toFloat(), (roundedRad).toFloat()
            ) // TR
            batch.draw(
                if (Corner.BOTTOM_RIGHT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX + rectW, rectY - rectH + roundedRad, (-roundedRad).toFloat(), (-roundedRad).toFloat()
            ) // BR
        }

        ColorStack.pop()
        batch.packedColor = lastPackedColor
    }
}