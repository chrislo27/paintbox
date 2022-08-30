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
 * @param useRows Whether to use rows instead of columns
 * @param putSpacersInBetweenLogicalCols If true, spacers are put inside each logical column. Otherwise, only between real columns. Recommended value is false
 */
abstract class AbstractColumnarContainer<Container : UIElement>(
    val columnAllotment: List<Int>, val useRows: Boolean, val putSpacersInBetweenLogicalCols: Boolean = false,
) : Pane() {

    val numLogicalColumns: Int
    val numRealColumns: Int get() = columnAllotment.size
    val numSpacers: Int get() = (columnAllotment.size - 1).coerceAtLeast(0)

    /**
     * The amount of usable area for columns, excluding [spacing].
     */
    protected val usableWidth: ReadOnlyFloatVar
    protected val logicalColumnsTally: List<Int> // Represents logical columns taken for each real column, used for spacer information
    
    val spacing: FloatVar = FloatVar(0f)
    val columnBoxes: List<Container>
    protected val spacersList: MutableList<UIElement?> = MutableList(numSpacers) { null }
    val spacers: List<UIElement?> get() = spacersList
    protected val spacerPositions: List<ReadOnlyFloatVar>

    init {
        columnAllotment.forEachIndexed { index, item ->
            // Check if any columns are less than or equal to zero
            if (item <= 0) {
                error("columnAllotment[$index] = $item was less than or equal to zero, must be positive")
            }
        }
        numLogicalColumns = columnAllotment.sum()
        
        usableWidth = FloatVar {
            val spacing = spacing.use()
            val thisDimension = getThisDimensional().use()
            if (putSpacersInBetweenLogicalCols) {
                (thisDimension - (spacing * (numLogicalColumns - 1))).coerceAtLeast(1f)
            } else {
                (thisDimension - (spacing * (numRealColumns - 1))).coerceAtLeast(1f)
            }
        }
        
        val proportion = 1f / numLogicalColumns
        var colAccumulator = 0
        logicalColumnsTally = mutableListOf()
        columnBoxes = columnAllotment.mapIndexed { realColIndex, logicalCols ->
            val box = createBox().also { newBox ->
                val colsSoFar = colAccumulator
                
                if (putSpacersInBetweenLogicalCols) {
                    getDimensional(newBox).bind {
                        (usableWidth.use() * proportion * logicalCols) + (spacing.use() * (logicalCols - 1))
                    }
                    getPositional(newBox).bind {
                        (usableWidth.use() * proportion + spacing.use()) * colsSoFar
                    }
                } else {
                    getDimensional(newBox).bind {
                        (usableWidth.use() * proportion * logicalCols)
                    }
                    getPositional(newBox).bind {
                        (usableWidth.use() * proportion) * colsSoFar + (spacing.use() * realColIndex)
                    }
                }
                
                onContainerCreate(newBox, colsSoFar, realColIndex)
                
                colAccumulator += logicalCols
            }
            logicalColumnsTally.add(colAccumulator)
            box
        }
        columnBoxes.forEach { 
            addChild(it)
        }
        spacerPositions = List(numSpacers) { spacerIndex ->
            FloatVar {
                val logicalCols = logicalColumnsTally[spacerIndex]
                if (putSpacersInBetweenLogicalCols) {
                    (usableWidth.use() * logicalCols / numLogicalColumns) + (spacing.use() * (logicalCols - 1))
                } else {
                    val box = columnBoxes[spacerIndex]
                    getPositional(box).use() + getDimensional(box).use()
                }
            }
        }
    }
    
    constructor(numColumns: Int, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : this(ListOfOnes(numColumns), useRows, putSpacersInBetweenLogicalCols)

    protected abstract fun onContainerCreate(newBox: Container, logicalIndex: Int, realIndex: Int)

    protected abstract fun createBox(): Container

    /**
     * Sets the spacer element. This will change the [element]'s bounds
     * [dimensionally][getDimensional] and [positionally][getPositional] if not null.
     * If [element] is null, this removes the existing element, if any.
     * 
     * @return The last element in that [spacer position][spacerIndex]
     * @see numSpacers
     */
    fun setSpacer(spacerIndex: Int, element: UIElement?): UIElement? {
        require(spacerIndex in 0 until numSpacers) { "spacerIndex out of bounds, got $spacerIndex, must be in [0, $numSpacers)" }
        
        val last = spacersList[spacerIndex]
        if (last != null) {
            removeChild(last)
        }
        if (element != null) {
            addChild(element)
            getDimensional(element).bind { spacing.use() }
            getPositional(element).bind { spacerPositions[spacerIndex].use() }
        }
        
        spacersList[spacerIndex] = element
        return last
    }
    
    fun getSpacer(spacerIndex: Int): UIElement? {
        require(spacerIndex in 0 until numSpacers) { "spacerIndex out of bounds, got $spacerIndex, must be in [0, $numSpacers)" }
        return spacersList[spacerIndex]
    }
    
    inline fun setAllSpacers(getter: (index: Int) -> UIElement?) {
        (0 until numSpacers).forEach { idx ->
            setSpacer(idx, getter(idx))
        }
    }

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
    
    constructor(columnAllotment: List<Int>, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(columnAllotment, useRows, putSpacersInBetweenLogicalCols)
    constructor(numColumns: Int, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(numColumns, useRows, putSpacersInBetweenLogicalCols)


    protected abstract fun getDefaultAlignment(logicalIndex: Int, realIndex: Int, logicalCols: Int, totalCols: Int): AlignEnum

    override fun onContainerCreate(newBox: Box, logicalIndex: Int, realIndex: Int) {
        newBox.align.set(getDefaultAlignment(logicalIndex, realIndex, numLogicalColumns, numRealColumns))
    }
}

open class ColumnarHBox : ColumnarBox<HBox, HBox.Align> {
    
    constructor(columnAllotment: List<Int>, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(columnAllotment, useRows, putSpacersInBetweenLogicalCols)
    constructor(numColumns: Int, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(numColumns, useRows, putSpacersInBetweenLogicalCols)

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
    
    constructor(columnAllotment: List<Int>, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(columnAllotment, useRows, putSpacersInBetweenLogicalCols)
    constructor(numColumns: Int, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(numColumns, useRows, putSpacersInBetweenLogicalCols)
    
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

open class ColumnarPane : AbstractColumnarContainer<Pane> {
    
    constructor(columnAllotment: List<Int>, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(columnAllotment, useRows, putSpacersInBetweenLogicalCols)
    constructor(numColumns: Int, useRows: Boolean, putSpacersInBetweenLogicalCols: Boolean = false)
            : super(numColumns, useRows, putSpacersInBetweenLogicalCols)

    override fun onContainerCreate(newBox: Pane, logicalIndex: Int, realIndex: Int) {
    }

    override fun createBox(): Pane {
        return Pane()
    }
}
