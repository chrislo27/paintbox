package paintbox.ui2

import paintbox.binding.FloatVar
import paintbox.ui2.LayoutDimensions.Companion.USE_COMPUTED_SIZE
import paintbox.ui2.LayoutDimensions.Companion.USE_PREF_SIZE
import kotlin.math.max
import kotlin.math.min


/**
 * A [UIElement] that supports [LayoutDimensions]'s min/pref/max width/height properties via the [layoutDimensions] property.
 */
open class LayoutElement : UIElement(), LayoutDimensions {
    
    companion object {

        fun boundedSize(value: Float, min: Float, max: Float): Float {
            // if max < value, return max
            // if min > value, return min
            // if min > max, return min
            return min(max(value, min), max(min, max))
        }
    }

    val layoutDimensions: LayoutDimensions = LayoutDimensionsImpl()
    
    //region LayoutDimensions's delegates
    override val minimumWidth: FloatVar get() = layoutDimensions.minimumWidth
    override val preferredWidth: FloatVar get() = layoutDimensions.preferredWidth
    override val maximumWidth: FloatVar get() = layoutDimensions.maximumWidth

    override val minimumHeight: FloatVar get() = layoutDimensions.minimumHeight
    override val preferredHeight: FloatVar get() = layoutDimensions.preferredHeight
    override val maximumHeight: FloatVar get() = layoutDimensions.maximumHeight
    //endregion

    //region Functions to compute min/pref/max width/height
    
    protected open fun computePrefWidth(height: Float): Float {
        // Default impl: pref width of the children
        var minX = 0f
        var maxX = 0f
        for (child in children.getOrCompute()) {
            val childX = child.bounds.x.get()
            minX = min(minX, childX)
            maxX = max(maxX, childX + boundedSize(child.prefWidth(USE_COMPUTED_SIZE), child.minWidth(USE_COMPUTED_SIZE), child.maxWidth(USE_COMPUTED_SIZE)))
        }
        return maxX - minX
    }
    
    protected open fun computeMinWidth(height: Float): Float {
        return prefWidth(height)
    }
    
    protected open fun computeMaxWidth(height: Float): Float {
        return Float.MAX_VALUE
    }
    
    protected open fun computePrefHeight(width: Float): Float {
        // Default impl: pref height of the children
        var minY = 0f
        var maxY = 0f
        for (child in children.getOrCompute()) {
            val childY = child.bounds.y.get()
            minY = min(minY, childY)
            maxY = max(maxY, childY + boundedSize(child.prefHeight(USE_COMPUTED_SIZE), child.minHeight(USE_COMPUTED_SIZE), child.maxHeight(USE_COMPUTED_SIZE)))
        }
        return maxY - minY
    }
    
    protected open fun computeMinHeight(width: Float): Float {
        return prefHeight(width)
    }
    
    protected open fun computeMaxHeight(width: Float): Float {
        return Float.MAX_VALUE
    }
    
    //endregion
    
    //region Overrides for getting min/pref/max width/height
    
    final override fun prefWidth(height: Float): Float {
        return when (val override = layoutDimensions.preferredWidth.get()) {
            USE_COMPUTED_SIZE -> computePrefWidth(height)
            else -> override.valueOrZero()
        }
    }

    final override fun minWidth(height: Float): Float {
        return when (val override = layoutDimensions.minimumWidth.get()) {
            USE_COMPUTED_SIZE -> computeMinWidth(height)
            USE_PREF_SIZE -> prefWidth(height)
            else -> override.valueOrZero()
        }
    }

    final override fun maxWidth(height: Float): Float {
        return when (val override = layoutDimensions.maximumWidth.get()) {
            USE_COMPUTED_SIZE -> computeMaxWidth(height)
            USE_PREF_SIZE -> prefWidth(height)
            else -> override.valueOrZero()
        }
    }

    final override fun prefHeight(width: Float): Float {
        return when (val override = layoutDimensions.preferredHeight.get()) {
            USE_COMPUTED_SIZE -> computePrefHeight(width)
            else -> override.valueOrZero()
        }
    }

    final override fun minHeight(width: Float): Float {
        return when (val override = layoutDimensions.minimumHeight.get()) {
            USE_COMPUTED_SIZE -> computeMinHeight(width)
            USE_PREF_SIZE -> prefHeight(width)
            else -> override.valueOrZero()
        }
    }

    final override fun maxHeight(width: Float): Float {
        return when (val override = layoutDimensions.maximumHeight.get()) {
            USE_COMPUTED_SIZE -> computeMaxHeight(width)
            USE_PREF_SIZE -> prefHeight(width)
            else -> override.valueOrZero()
        }
    }
    
    //endregion
    
}
