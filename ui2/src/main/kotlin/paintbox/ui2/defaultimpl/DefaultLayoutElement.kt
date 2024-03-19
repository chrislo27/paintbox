package paintbox.ui2.defaultimpl

import paintbox.ui2.LayoutDimensions
import paintbox.ui2.LayoutElement
import paintbox.ui2.UIElement


open class DefaultLayoutElement(
    uiElement: UIElement = DefaultUIElement(),
) : LayoutElement, UIElement by uiElement, LayoutDimensions {

    override val layoutDimensions: LayoutDimensions = LayoutDimensionsImpl()


    override fun prefWidth(height: Float): Float = super<LayoutElement>.prefWidth(height)
    override fun minWidth(height: Float): Float = super<LayoutElement>.minWidth(height)
    override fun maxWidth(height: Float): Float = super<LayoutElement>.maxWidth(height)

    override fun prefHeight(width: Float): Float = super<LayoutElement>.prefHeight(width)
    override fun minHeight(width: Float): Float = super<LayoutElement>.minHeight(width)
    override fun maxHeight(width: Float): Float = super<LayoutElement>.maxHeight(width)

}
