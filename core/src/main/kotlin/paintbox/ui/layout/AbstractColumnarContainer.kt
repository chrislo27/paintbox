package paintbox.ui.layout

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar
import paintbox.ui.Pane
import paintbox.ui.UIElement


/**
 * An abstract pane that can have 1 or more columns or rows.
 */
abstract class AbstractColumnarContainer<Container : UIElement>(
    val numColumns: Int, val useRows: Boolean,
) : Pane() {

    val spacing: FloatVar = FloatVar(0f)
    val columnBoxes: List<Container>

    init {
        val proportion = 1f / numColumns
        columnBoxes = (0 until numColumns).map { index ->
            createBox().also { newBox ->
                val boxDimensional = getDimensional(newBox)
                boxDimensional.bind {
                    val spacing = spacing.use()
                    val thisDimension = getThisDimensional().use()
                    val usableWidth = (thisDimension - (spacing * (numColumns - 1))).coerceAtLeast(1f)
                    usableWidth * proportion
                }
                getPositional(newBox).bind { 
                    (boxDimensional.use() + spacing.use()) * index
                }
                onCreate(newBox, index)
            }
        }
        columnBoxes.forEach { 
            addChild(it)
        }
    }

    protected abstract fun onCreate(newBox: Container, index: Int)

    protected abstract fun createBox(): Container

    /**
     * Returns either the width or height var for this columnar from [contentZone].
     */
    protected fun getThisDimensional(): ReadOnlyFloatVar {
        return if (useRows) this.contentZone.height else this.contentZone.width
    }

    /**
     * Returns either the width or height var for an element.
     */
    protected fun getDimensional(element: UIElement): FloatVar {
        return if (useRows) element.bounds.height else element.bounds.width
    }

    /**
     * Returns either the x or y var for an element.
     */
    protected fun getPositional(element: UIElement): FloatVar {
        return if (useRows) element.bounds.y else element.bounds.x
    }
    
    operator fun get(index: Int): Container = columnBoxes[index]

}

/**
 * An [AbstractColumnarContainer] whose container is an [AbstractHVBox].
 */
abstract class ColumnarBox<Box : AbstractHVBox<AlignEnum>, AlignEnum : AbstractHVBox.BoxAlign>(
    numColumns: Int, useRows: Boolean
) : AbstractColumnarContainer<Box>(numColumns, useRows) {

    protected abstract fun getDefaultAlignment(index: Int, total: Int): AlignEnum

    override fun onCreate(newBox: Box, index: Int) {
        newBox.align.set(getDefaultAlignment(index, numColumns))
    }
}

open class ColumnarHBox(numColumns: Int, useRows: Boolean) : ColumnarBox<HBox, HBox.Align>(numColumns, useRows) {
    override fun createBox(): HBox {
        return HBox()
    }

    override fun getDefaultAlignment(index: Int, total: Int): HBox.Align {
        return when (total) {
            1 -> HBox.Align.CENTRE
            2 -> if (index == 0) HBox.Align.LEFT else HBox.Align.RIGHT
            else -> if (index == 0) HBox.Align.LEFT else if (index == total - 1) HBox.Align.RIGHT else HBox.Align.CENTRE
        }
    }
}


open class ColumnarVBox(numColumns: Int, useRows: Boolean) : ColumnarBox<VBox, VBox.Align>(numColumns, useRows) {
    override fun createBox(): VBox {
        return VBox()
    }

    override fun getDefaultAlignment(index: Int, total: Int): VBox.Align {
        return when (total) {
            1 -> VBox.Align.CENTRE
            2 -> if (index == 0) VBox.Align.TOP else VBox.Align.BOTTOM
            else -> if (index == 0) VBox.Align.TOP else if (index == total - 1) VBox.Align.BOTTOM else VBox.Align.CENTRE
        }
    }
}
