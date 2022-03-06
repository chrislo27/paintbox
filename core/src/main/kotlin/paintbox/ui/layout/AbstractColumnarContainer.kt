package paintbox.ui.layout

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar
import paintbox.ui.Pane
import paintbox.ui.UIElement
import paintbox.util.ListOfOnes


/**
 * An abstract pane that can have 1 or more columns or rows.
 * 
 * @param columnAllotment A list of positive integers indicating how many logical columns each real column takes up
 */
abstract class AbstractColumnarContainer<Container : UIElement>(
    val columnAllotment: List<Int>, val useRows: Boolean
) : Pane() {

    val numLogicalColumns: Int
    val numRealColumns: Int get() = columnAllotment.size
    val spacing: FloatVar = FloatVar(0f)
    val columnBoxes: List<Container>

    init {
        columnAllotment.forEachIndexed { index, item ->
            if (item <= 0) {
                error("columnAllotment[$index] = $item was less than or equal to zero, must be positive")
            }
        }
        numLogicalColumns = columnAllotment.sum()
        
        val usableWidth = FloatVar {
            val spacing = spacing.use()
            val thisDimension = getThisDimensional().use()
            (thisDimension - (spacing * (numLogicalColumns - 1))).coerceAtLeast(1f)
        }
        val proportion = 1f / numLogicalColumns
        var colAccumulator = 0
        columnBoxes = columnAllotment.mapIndexed { index, logicalCols ->
            createBox().also { newBox ->
                val colsSoFar = colAccumulator
                getDimensional(newBox).bind {
                    (usableWidth.use() * proportion * logicalCols) + (spacing.use() * (logicalCols - 1))
                }
                getPositional(newBox).bind {
                    (usableWidth.use() * proportion + spacing.use()) * colsSoFar
                }
                onCreate(newBox, colsSoFar, index)
                
                colAccumulator += logicalCols
            }
        }
        columnBoxes.forEach { 
            addChild(it)
        }
    }
    
    constructor(numColumns: Int, useRows: Boolean) : this(ListOfOnes(numColumns), useRows)

    protected abstract fun onCreate(newBox: Container, logicalIndex: Int, realIndex: Int)

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
abstract class ColumnarBox<Box : AbstractHVBox<AlignEnum>, AlignEnum : AbstractHVBox.BoxAlign>
    : AbstractColumnarContainer<Box> {
    
    constructor(columnAllotment: List<Int>, useRows: Boolean) : super(columnAllotment, useRows)
    constructor(numColumns: Int, useRows: Boolean) : super(numColumns, useRows)


    protected abstract fun getDefaultAlignment(logicalIndex: Int, realIndex: Int, logicalCols: Int, totalCols: Int): AlignEnum

    override fun onCreate(newBox: Box, logicalIndex: Int, realIndex: Int) {
        newBox.align.set(getDefaultAlignment(logicalIndex, realIndex, numLogicalColumns, numRealColumns))
    }
}

open class ColumnarHBox : ColumnarBox<HBox, HBox.Align> {
    
    constructor(columnAllotment: List<Int>, useRows: Boolean) : super(columnAllotment, useRows)
    constructor(numColumns: Int, useRows: Boolean) : super(numColumns, useRows)

    override fun createBox(): HBox {
        return HBox()
    }

    override fun getDefaultAlignment(logicalIndex: Int, realIndex: Int, logicalCols: Int, totalCols: Int): HBox.Align {
        return when (totalCols) {
            1 -> HBox.Align.CENTRE
            2 -> if (realIndex == 0) HBox.Align.LEFT else HBox.Align.RIGHT
            else -> if (realIndex == 0) HBox.Align.LEFT else if (realIndex == totalCols - 1) HBox.Align.RIGHT else HBox.Align.CENTRE
        }
    }
}


open class ColumnarVBox : ColumnarBox<VBox, VBox.Align> {
    
    constructor(columnAllotment: List<Int>, useRows: Boolean) : super(columnAllotment, useRows)
    constructor(numColumns: Int, useRows: Boolean) : super(numColumns, useRows)
    
    override fun createBox(): VBox {
        return VBox()
    }

    override fun getDefaultAlignment(logicalIndex: Int, realIndex: Int, logicalCols: Int, totalCols: Int): VBox.Align {
        return when (totalCols) {
            1 -> VBox.Align.CENTRE
            2 -> if (realIndex == 0) VBox.Align.TOP else VBox.Align.BOTTOM
            else -> if (realIndex == 0) VBox.Align.TOP else if (realIndex == totalCols - 1) VBox.Align.BOTTOM else VBox.Align.CENTRE
        }
    }
}
