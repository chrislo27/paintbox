package paintbox.ui.skin

import com.badlogic.gdx.graphics.g2d.Batch
import paintbox.ui.UIElement


/**
 * Renders a [Skinnable] [UIElement].
 * Each [Skin] shall only be responsible for rendering one specific [Skinnable].
 */
abstract class Skin<Element>(open val element: Element) {

    abstract fun renderSelf(originX: Float, originY: Float, batch: Batch)

    abstract fun renderSelfAfterChildren(originX: Float, originY: Float, batch: Batch)

}

fun interface SkinFactory<ElementType, out S : Skin<ElementType>, in SkinnableType : Skinnable<ElementType>> {

    fun createSkin(element: SkinnableType): S

}
