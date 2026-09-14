package paintbox.ui.border

import com.badlogic.gdx.graphics.g2d.Batch
import paintbox.ui.UIElement


interface Border {

    fun renderBorder(originX: Float, originY: Float, batch: Batch, element: UIElement)
    
    fun renderBorderBeforeSelf(originX: Float, originY: Float, batch: Batch, element: UIElement) {
    }

}