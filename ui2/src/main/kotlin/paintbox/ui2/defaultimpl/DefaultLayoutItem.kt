package paintbox.ui2.defaultimpl

import paintbox.ui2.LayoutDimensions
import paintbox.ui2.LayoutItem
import paintbox.ui2.UIElement


open class DefaultLayoutItem(
    uiElement: UIElement = DefaultUIElement(),
) : LayoutItem, UIElement by uiElement, LayoutDimensions {

    override val layoutDimensions: LayoutDimensions = LayoutDimensionsImpl()


    override fun prefWidth(height: Float): Float = super<LayoutItem>.prefWidth(height)
    override fun minWidth(height: Float): Float = super<LayoutItem>.minWidth(height)
    override fun maxWidth(height: Float): Float = super<LayoutItem>.maxWidth(height)

    override fun prefHeight(width: Float): Float = super<LayoutItem>.prefHeight(width)
    override fun minHeight(width: Float): Float = super<LayoutItem>.minHeight(width)
    override fun maxHeight(width: Float): Float = super<LayoutItem>.maxHeight(width)

}
