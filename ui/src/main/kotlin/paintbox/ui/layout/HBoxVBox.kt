package paintbox.ui.layout

import paintbox.binding.*
import paintbox.ui.Pane
import paintbox.ui.UIElement
import paintbox.ui.area.Bounds
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * An abstract pane that lays out its children. Use [HBox] and [VBox] for implementations.
 */
abstract class AbstractHVBox<AlignEnum : AbstractHVBox.BoxAlign> : AbstractLayoutPane() {

    /*
    The element data cache stores a particular dimension of an element and the accumulated value. This allows
    for optimal adjustment when elements are added/removed/edited.
    
    When the attemptLayout(index) function is called, all the elements at the given index and
    after have their positions recalculate and updated. If the InternalAlignment is MIDDLE or MAX, every element
    will be updated (not necessarily recalculated).
     */

    interface BoxAlign {

        val internalAlignment: InternalAlignment
    }

    protected inner class ElementData(val element: UIElement, var index: Int, var length: Float) {

        var start: Float = 0f
        var nextSpacing: Float = 0f

        private val sizeListener: VarChangedListener<Float> = VarChangedListener {
            this@AbstractHVBox.attemptLayout(index)
        }
        
        fun addListenersToElement() {
            val dimensional = this@AbstractHVBox.getDimensional(element)
            dimensional.addListener(this.sizeListener)
        }
        
        fun removeListenersFromElement() {
            val dimensional = this@AbstractHVBox.getDimensional(element)
            dimensional.removeListener(this.sizeListener)
        }
    }

    protected val elementCache: MutableList<ElementData> = mutableListOf()

    /**
     * The spacing in between children.
     */
    val spacing: FloatVar = FloatVar(0f)

    /**
     * A flag to disable layouts.
     * This is useful for pausing layout computation until all children have been added.
     */
    val disableLayouts: BooleanVar = BooleanVar(false)

    /**
     * If true, when [disableLayouts] is set back to false, [reLayout] is called.
     */
    val doLayoutIfLayoutsReenabled: BooleanVar = BooleanVar(true)

    /**
     * If true, this box will call [sizeWidthToChildren]/[sizeHeightToChildren] whenever its children's sizes change.
     */
    val autoSizeToChildren: BooleanVar = BooleanVar(false)

    /**
     * The minimum size to use when [autoSizeToChildren] is true. Default zero.
     */
    val autoSizeMinimumSize: FloatVar = FloatVar(0f)

    /**
     * The maximum size to use when [autoSizeToChildren] is true. Default positive infinity.
     */
    val autoSizeMaximumSize: FloatVar = FloatVar(Float.POSITIVE_INFINITY)

    /**
     * If true, elements act as if they were in reverse order.
     */
    val reverseLayout: BooleanVar = BooleanVar(false)

    protected val internalAlignment: Var<InternalAlignment> = Var(InternalAlignment.MIN)
    private var isDoingLayout: Boolean = false

    abstract val align: Var<AlignEnum>

    init {
        spacing.addListener {
            reLayout()
        }
        internalAlignment.addListener {
            reLayout()
        }
        reverseLayout.addListener {
            reLayout()
        }
        disableLayouts.addListener {
            if (doLayoutIfLayoutsReenabled.get()) {
                reLayout()
            }
        }
        getThisDimensional().addListener {
            if (internalAlignment.getOrCompute().ratio > 0f) {
                reLayout()
            }
        }

        autoSizeToChildren.addListener {
            if (it.getOrCompute()) {
                doAutosize()
            }
        }
        autoSizeMinimumSize.addListener { minSize ->
            if (this.getThisDimensional().get() < minSize.getOrCompute()) {
                autosizeIfNecessary()
            }
        }
        autoSizeMaximumSize.addListener { maxSize ->
            if (this.getThisDimensional().get() > maxSize.getOrCompute()) {
                autosizeIfNecessary()
            }
        }
    }

    /**
     * Returns either the width or height var for this box from [contentZone].
     */
    protected abstract fun getThisDimensional(): ReadOnlyFloatVar

    /**
     * Returns either the width or height var for an element.
     */
    protected abstract fun getDimensional(element: UIElement): FloatVar

    /**
     * Returns either the x or y var for an element.
     */
    protected abstract fun getPositional(element: UIElement): FloatVar

    /**
     * Called when autosizing is attempted. Should call [sizeWidthToChildren] or [sizeHeightToChildren].
     */
    abstract fun doAutosize()
    
    fun autosizeIfNecessary() {
        if (autoSizeToChildren.get()) {
            doAutosize()
        }
    }

    /**
     * Sets [disableLayouts] to true, runs the [func], then sets [disableLayouts] to false
     * and does a [full layout][reLayout].
     *
     * This is intended as an optimization when adding a set of children to avoid constant layout recomputations.
     */
    @OptIn(ExperimentalContracts::class)
    inline fun temporarilyDisableLayouts(func: () -> Unit) {
        contract {
            callsInPlace(func, InvocationKind.EXACTLY_ONCE)
        }
        disableLayouts.set(true)
        func()
        disableLayouts.set(false)
    }

    /**
     * Attempts to do a full layout.
     */
    fun reLayout() {
        attemptLayout(0)
    }

    protected fun attemptLayout(index: Int) {
        if (isDoingLayout || disableLayouts.get()) return

        if (index >= elementCache.size) {
            autosizeIfNecessary()
            return
        }

        isDoingLayout = true
        try {
            val idx = index
            val cache = elementCache.toList()
            var posAccumulator = if (idx > 0) (cache[idx - 1].let { it.start + it.length + it.nextSpacing }) else 0f
            val cacheSize = cache.size
            val spacingValue = spacing.get()

            for (i in idx..<cacheSize) {
                val d = cache[i]
                val element = d.element
                d.start = posAccumulator
                d.length = getDimensional(element).get()
                d.nextSpacing = spacingValue

                val pos = getPositional(element)
                pos.set(d.start)

                posAccumulator += d.length
                if (i < cacheSize - 1) {
                    posAccumulator += d.nextSpacing
                }
            }
            
            // Alignment and reverse layout
            // Reverse layout: Flip along the cross axis (i.e. negate direction) and offset back into positive
            val align = this.internalAlignment.getOrCompute()
            val doReverseLayout = reverseLayout.get()
            val totalSize = cache.last().let { it.start + it.length }
            val thisSize = getThisDimensional().get()

            val alignmentOffset: Float = when (align) {
                InternalAlignment.MIN -> 0f
                InternalAlignment.MIDDLE -> (thisSize - totalSize) / 2f
                InternalAlignment.MAX -> (thisSize - totalSize)
            }

            for (i in 0..<cacheSize) {
                val d = cache[i]
                val element = d.element
                val pos = getPositional(element)

                val layoutPos = if (doReverseLayout) (-d.start - d.length + totalSize) else (d.start)
                pos.set(layoutPos + alignmentOffset)
            }

            autosizeIfNecessary()
        } finally {
            isDoingLayout = false
        }
    }

    override fun onChildAdded(newChild: UIElement, atIndex: Int) {
        super.onChildAdded(newChild, atIndex)

        // Add to correct position in cache based on atIndex
        val currentCache = elementCache.toList()
        val dimensional = getDimensional(newChild)
        val elementData = ElementData(newChild, currentCache.size, dimensional.get())
        elementData.addListenersToElement()
        elementCache.add(atIndex, elementData)
        attemptLayout((atIndex).coerceAtLeast(0))
    }

    override fun onChildRemoved(oldChild: UIElement, oldIndex: Int) {
        super.onChildRemoved(oldChild, oldIndex)

        // Find where in the cache it was deleted and update the subsequent ones
        val currentCache = elementCache.toList()
        // Use given oldIndex if correct. Use old logic as fallback
        val index = oldIndex.takeIf { currentCache.getOrNull(oldIndex)?.element == oldChild }
            ?: currentCache.indexOfFirst { it.element == oldChild }
        if (index < 0) return

        val removedData = elementCache.removeAt(index)
        removedData.removeListenersFromElement()

        for (i in (index + 1)..<currentCache.size) {
            currentCache[i].index = i - 1
        }
        attemptLayout(index)
    }
}

/**
 * A [Pane] that lays out its children from left to right. Children of this [HBox] should expect their
 * [bounds.x][Bounds.x] to be changed, and should NOT have their width depend on their own x.
 */
open class HBox : AbstractHVBox<HBox.Align>() {

    enum class Align(override val internalAlignment: InternalAlignment) : BoxAlign {
        LEFT(InternalAlignment.MIN), CENTRE(InternalAlignment.MIDDLE), RIGHT(InternalAlignment.MAX);
    }

    /**
     * Alias for [reverseLayout].
     */
    val rightToLeft: BooleanVar get() = super.reverseLayout
    override val align: Var<Align> = Var(Align.LEFT)

    init {
        this.internalAlignment.bind {
            align.use().internalAlignment
        }
        this.bounds.width.addListener {
            attemptLayout(0)
        }
    }

    override fun getDimensional(element: UIElement): FloatVar {
        return element.bounds.width
    }

    override fun getPositional(element: UIElement): FloatVar {
        return element.bounds.x
    }

    override fun getThisDimensional(): ReadOnlyFloatVar {
        return this.contentZone.width
    }

    override fun doAutosize() {
        sizeWidthToChildren(autoSizeMinimumSize.get(), autoSizeMaximumSize.get())
    }

    override fun sizeWidthToChildren(minimumWidth: Float, maximumWidth: Float): Float {
        var width = 0f
        
        val children = this.children.getOrCompute()
        if (children.isNotEmpty()) {
            // In an HBox, the last child in the flow determines the width (left to right -> last, right to left -> first)
            val firstmost = if (rightToLeft.get()) children.last() else children.first()
            val lastmost = if (rightToLeft.get()) children.first() else children.last()
            width = lastmost.bounds.x.get() - firstmost.bounds.x.get() + lastmost.bounds.width.get()
        }

        val borderInsets = this.border.getOrCompute()
        val marginInsets = this.margin.getOrCompute()
        val paddingInsets = this.padding.getOrCompute()

        width += borderInsets.leftAndRight() + marginInsets.leftAndRight() + paddingInsets.leftAndRight()

        val computedWidth = width.coerceIn(minimumWidth, maximumWidth)
        this.bounds.width.set(computedWidth)
        return computedWidth
    }
}

/**
 * A [Pane] that lays out its children from top to bottom. Children of this [VBox] should expect their
 * [bounds.y][Bounds.y] to be changed, and should NOT have their height depend on their own y.
 */
open class VBox : AbstractHVBox<VBox.Align>() {

    enum class Align(override val internalAlignment: InternalAlignment) : BoxAlign {
        TOP(InternalAlignment.MIN), CENTRE(InternalAlignment.MIDDLE), BOTTOM(InternalAlignment.MAX);
    }

    /**
     * Alias for [reverseLayout].
     */
    val bottomToTop: BooleanVar get() = super.reverseLayout
    override val align: Var<Align> = Var(Align.TOP)

    init {
        this.internalAlignment.bind {
            align.use().internalAlignment
        }
        this.bounds.height.addListener {
            attemptLayout(0)
        }
    }

    override fun getDimensional(element: UIElement): FloatVar {
        return element.bounds.height
    }

    override fun getPositional(element: UIElement): FloatVar {
        return element.bounds.y
    }

    override fun getThisDimensional(): ReadOnlyFloatVar {
        return this.contentZone.height
    }

    override fun doAutosize() {
        sizeHeightToChildren(autoSizeMinimumSize.get(), autoSizeMaximumSize.get())
    }

    override fun sizeHeightToChildren(minimumHeight: Float, maximumHeight: Float): Float {
        // In an VBox, the last child in the flow determines the width (top to bottom -> last, bottom to top -> first)
        var height = 0f
        val children = this.children.getOrCompute()
        if (children.isNotEmpty()) {
            val firstmost = if (bottomToTop.get()) children.last() else children.first()
            val lastmost = if (bottomToTop.get()) children.first() else children.last()
            height = lastmost.bounds.y.get() - firstmost.bounds.y.get() + lastmost.bounds.height.get()
        }

        val borderInsets = this.border.getOrCompute()
        val marginInsets = this.margin.getOrCompute()
        val paddingInsets = this.padding.getOrCompute()

        height += borderInsets.topAndBottom() + marginInsets.topAndBottom() + paddingInsets.topAndBottom()

        val computedHeight = height.coerceIn(minimumHeight, maximumHeight)
        this.bounds.height.set(computedHeight)
        return computedHeight
    }
}