package paintbox.ui2.defaultimpl

import paintbox.binding.FloatVar
import paintbox.ui2.LayoutDimensions

class LayoutDimensionsImpl : LayoutDimensions {
    
    override val minWidth: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val prefWidth: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val maxWidth: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)

    override val minHeight: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val prefHeight: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val maxHeight: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
}