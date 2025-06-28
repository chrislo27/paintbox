package paintbox.ui.element

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import paintbox.binding.ContextBinding
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.ui.UIElement
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect


open class RectElement(initColor: Color) : UIElement() {

    val color: Var<Color> = Var(Color(1f, 1f, 1f, 1f).set(initColor))

    constructor() : this(Color.WHITE)

    constructor(binding: ContextBinding<Color>) : this() {
        color.bind(binding)
    }

    constructor(bindable: ReadOnlyVar<Color>) : this() {
        color.bind(bindable)
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val renderBounds = this.paddingZone
        val x = renderBounds.x.get() + originX
        val y = originY - renderBounds.y.get()
        val w = renderBounds.width.get()
        val h = renderBounds.height.get()
        val lastPackedColor = batch.packedColor

        val opacity: Float = this.apparentOpacity.get()
        val tmpColor: Color = ColorStack.getAndPush()
        tmpColor.set(color.getOrCompute())
        tmpColor.a *= opacity
        batch.color = tmpColor
        batch.fillRect(x, y - h, w, h)

        ColorStack.pop()
        batch.packedColor = lastPackedColor
    }
}