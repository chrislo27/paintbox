package paintbox.ui.element

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import paintbox.PaintboxGame
import paintbox.binding.ContextBinding
import paintbox.binding.FloatVar
import paintbox.binding.Var
import paintbox.ui.UIElement
import paintbox.util.ColorStack
import paintbox.util.gdxutils.drawQuad


/**
 * A quad describes a OpenGL quad with configurable colours for each corner.
 */
open class QuadElement(initTopLeft: Color, initTopRight: Color, initBottomLeft: Color, initBottomRight: Color) :
    UIElement() {

    val topLeftColor: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initTopLeft))
    val topRightColor: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initTopRight))
    val bottomLeftColor: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initBottomLeft))
    val bottomRightColor: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initBottomRight))

    /**
     * Texture to use when filling the quad. By default it is null, which means it will use [paintbox.PaintboxGame.fillTexture].
     */
    val texture: Var<Texture?> = Var(null)

    val topLeftOffsetU: FloatVar = FloatVar(0f)
    val topLeftOffsetV: FloatVar = FloatVar(1f)
    val topRightOffsetU: FloatVar = FloatVar(1f)
    val topRightOffsetV: FloatVar = FloatVar(1f)
    val bottomLeftOffsetU: FloatVar = FloatVar(0f)
    val bottomLeftOffsetV: FloatVar = FloatVar(0f)
    val bottomRightOffsetU: FloatVar = FloatVar(1f)
    val bottomRightOffsetV: FloatVar = FloatVar(0f)

    val topLeftTextureU: FloatVar = FloatVar { topLeftOffsetU.use() }
    val topLeftTextureV: FloatVar = FloatVar { topLeftOffsetV.use() }
    val topRightTextureU: FloatVar = FloatVar { topRightOffsetU.use() }
    val topRightTextureV: FloatVar = FloatVar { topRightOffsetV.use() }
    val bottomLeftTextureU: FloatVar = FloatVar { bottomLeftOffsetU.use() }
    val bottomLeftTextureV: FloatVar = FloatVar { bottomLeftOffsetV.use() }
    val bottomRightTextureU: FloatVar = FloatVar { bottomRightOffsetU.use() }
    val bottomRightTextureV: FloatVar = FloatVar { bottomRightOffsetV.use() }

    constructor(initColorAll: Color) : this(initColorAll, initColorAll, initColorAll, initColorAll)
    constructor() : this(Color.WHITE)

    constructor(bindingAll: ContextBinding<Color>) : this() {
        topLeftColor.bind(bindingAll)
        topRightColor.bind(bindingAll)
        bottomLeftColor.bind(bindingAll)
        bottomRightColor.bind(bindingAll)
    }

    fun leftRightGradient(left: Color, right: Color) {
        topLeftColor.set(left.cpy())
        bottomLeftColor.set(left.cpy())
        topRightColor.set(right.cpy())
        bottomRightColor.set(right.cpy())
    }

    fun leftRightGradient(left: ContextBinding<Color>, right: ContextBinding<Color>) {
        topLeftColor.bind(left)
        bottomLeftColor.bind(left)
        topRightColor.bind(right)
        bottomRightColor.bind(right)
    }

    fun topBottomGradient(top: Color, bottom: Color) {
        topLeftColor.set(top.cpy())
        topRightColor.set(top.cpy())
        bottomLeftColor.set(bottom.cpy())
        bottomRightColor.set(bottom.cpy())
    }

    fun topBottomGradient(top: ContextBinding<Color>, bottom: ContextBinding<Color>) {
        topLeftColor.bind(top)
        topRightColor.bind(top)
        bottomLeftColor.bind(bottom)
        bottomRightColor.bind(bottom)
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val renderBounds = this.paddingZone
        val x = renderBounds.x.get() + originX
        val y = originY - renderBounds.y.get()
        val w = renderBounds.width.get()
        val h = renderBounds.height.get()
        val lastPackedColor = batch.packedColor

        val opacity: Float = this.apparentOpacity.get()
        val tmpColorTL: Color = ColorStack.getAndPush()
        tmpColorTL.set(topLeftColor.getOrCompute())
        tmpColorTL.a *= opacity
        val tmpColorTR: Color = ColorStack.getAndPush()
        tmpColorTR.set(topRightColor.getOrCompute())
        tmpColorTR.a *= opacity
        val tmpColorBL: Color = ColorStack.getAndPush()
        tmpColorBL.set(bottomLeftColor.getOrCompute())
        tmpColorBL.a *= opacity
        val tmpColorBR: Color = ColorStack.getAndPush()
        tmpColorBR.set(bottomRightColor.getOrCompute())
        tmpColorBR.a *= opacity

        batch.setColor(1f, 1f, 1f, 1f)
        batch.drawQuad(
            x + w * bottomLeftOffsetU.get(), (y - h) + h * bottomLeftOffsetV.get(), tmpColorBL.toFloatBits(),
            x + w * bottomRightOffsetU.get(), (y - h) + h * bottomRightOffsetV.get(), tmpColorBR.toFloatBits(),
            x + w * topRightOffsetU.get(), (y - h) + h * topRightOffsetV.get(), tmpColorTR.toFloatBits(),
            x + w * topLeftOffsetU.get(), (y - h) + h * topLeftOffsetV.get(), tmpColorTL.toFloatBits(),
            texture.getOrCompute() ?: PaintboxGame.gameInstance.staticAssets.fillTexture,
            blU = bottomLeftTextureU.get(), blV = bottomLeftTextureV.get(),
            brU = bottomRightTextureU.get(), brV = bottomRightTextureV.get(),
            trU = topRightTextureU.get(), trV = topRightTextureV.get(),
            tlU = topLeftTextureU.get(), tlV = topLeftTextureV.get()
        )

        ColorStack.pop()
        ColorStack.pop()
        ColorStack.pop()
        ColorStack.pop()
        batch.packedColor = lastPackedColor
    }
}