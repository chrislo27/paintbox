package paintbox.ui.border

import com.badlogic.gdx.graphics.g2d.Batch
import paintbox.ui.UIElement


object NoBorder : Border {

    override fun renderBorder(originX: Float, originY: Float, batch: Batch, element: UIElement) {
        // NO-OP
    }

    override fun renderBorderBeforeSelf(originX: Float, originY: Float, batch: Batch, element: UIElement) {
        // NO-OP
    }
}
