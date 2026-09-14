package paintbox.ui.element

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import paintbox.binding.ContextBinding
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.ui.UIElement
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

    override fun renderSelf(originX: Float, originY: Float, batch: Batch) {
        val renderBounds = this.paddingZone
        val x = renderBounds.x.get() + originX
        val y = originY - renderBounds.y.get()
        val w = renderBounds.width.get()
        val h = renderBounds.height.get()
        val lastPackedColor = batch.packedColor

        val opacity: Float = this.apparentOpacity.get()
        val c = color.getOrCompute()
        batch.setColor(c.r, c.g, c.b, c.a * opacity)
        batch.fillRect(x, y - h, w, h)

        batch.packedColor = lastPackedColor
    }
}