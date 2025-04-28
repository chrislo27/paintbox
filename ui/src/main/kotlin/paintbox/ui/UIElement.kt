package paintbox.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import paintbox.PaintboxGame
import paintbox.binding.*
import paintbox.font.PaintboxFont
import paintbox.ui.UIElement.Companion.clearDefaultFontOverride
import paintbox.ui.UIElement.Companion.defaultFont
import paintbox.ui.area.ReadOnlyBounds
import paintbox.ui.border.Border
import paintbox.ui.border.NoBorder
import paintbox.util.RectangleStack
import paintbox.util.gdxutils.intersects
import kotlin.math.max
import kotlin.math.min


open class UIElement : UIBounds() {

    companion object {

        private val DEFAULT_MULTIPLIER_BINDING: ContextBinding<Float> = { 1f }
        private const val CLIP_RECT_BUFFER: Float = 16f

        private var defaultFontOverride: PaintboxFont? = null

        /**
         * The default font to use in various controls.
         * Defaults to [PaintboxGame.gameInstance.defaultFonts.debugFont][PaintboxGame.defaultFonts.debugFont], but can be overridden.
         * @see clearDefaultFontOverride
         */
        var defaultFont: PaintboxFont
            get() = defaultFontOverride ?: PaintboxGame.gameInstance.defaultFonts.debugFont
            set(value) {
                defaultFontOverride = value
            }

        /**
         * Resets the [defaultFont] to not be overridden.
         */
        fun clearDefaultFontOverride() {
            defaultFontOverride = null
        }
    }

    val parent: Var<UIElement?> = Var(null)

    private val _children: Var<List<UIElement>> = Var(emptyList())
    val children: ReadOnlyVar<List<UIElement>> get() = _children

    val inputListeners: Var<List<InputEventListener>> = Var(emptyList())
    val sceneRoot: ReadOnlyVar<SceneRoot?> = Var {
        parent.use()?.sceneRoot?.use()
    }

    /**
     * If false, this element and its children will not be rendered.
     */
    val visible: BooleanVar = BooleanVar(true)
    val apparentVisibility: ReadOnlyBooleanVar = BooleanVar {
        visible.use() && (parent.use()?.apparentVisibility?.use() ?: true)
    }


    /**
     * If true, this element will render with [ScissorStack] clipping on its entire bounds.
     */
    val doClipping: BooleanVar = BooleanVar(false)

    /**
     * The opacity of this [UIElement].
     */
    val opacity: FloatVar = FloatVar(1f)

    /**
     * The [apparentOpacity] level of the [parent], if there is no parent then 1.0 is used.
     */
    val parentOpacity: ReadOnlyFloatVar = FloatVar {
        parent.use()?.apparentOpacity?.use() ?: 1f
    }

    /**
     * The opacity level of this [UIElement], taking into account the parent's opacity level using
     * `parentOpacity * this.opacity`.
     *
     * This is to be used by the rendering implementation.
     */
    val apparentOpacity: ReadOnlyFloatVar = FloatVar {
        parentOpacity.use() * opacity.use()
    }

    val borderStyle: Var<Border> = Var(NoBorder)

    init {
        bindWidthToParent(adjust = 0f, multiplier = 1f)
        bindHeightToParent(adjust = 0f, multiplier = 1f)

        @Suppress("LeakingThis")
        if (this is Focusable) {
            apparentVisibility.addListener {
                if (!it.getOrCompute()) requestUnfocus()
            }
        }
    }

    @Suppress("RedundantModalityModifier")
    final fun render(
        originX: Float, originY: Float, batch: SpriteBatch,
        currentClipRect: Rectangle, uiOriginX: Float, uiOriginY: Float,
    ) {
        if (!visible.get()) return

        val clip = doClipping.get()
        val childOriginBounds = this.contentZone
        val childOriginX = childOriginBounds.x.get()
        val childOriginY = childOriginBounds.y.get()
        // uiOriginX/Y represent where the 0,0 point is for the children in UI coordinates
        val newUIOriginX = uiOriginX + childOriginX
        val newUIOriginY = uiOriginY + childOriginY

        renderOptionallyWithClip(originX, originY, batch, clip) { _, _, _ ->
            this.renderSelf(originX, originY, batch)

            if (clip) {
                val suggestedClipX = newUIOriginX - this.contentOffsetX.get()
                val suggestedClipY = newUIOriginY - this.contentOffsetY.get()
                val suggestedClipWidth = childOriginBounds.width.get()
                val suggestedClipHeight = childOriginBounds.height.get()
                // Take the minimal area from the old clip and new rect
                val minX = max(currentClipRect.x, suggestedClipX)
                val minY = max(currentClipRect.y, suggestedClipY)
                val maxX = min(currentClipRect.x + currentClipRect.width, suggestedClipX + suggestedClipWidth)
                val maxY = min(currentClipRect.y + currentClipRect.height, suggestedClipY + suggestedClipHeight)
                val newClipRect = RectangleStack.getAndPush().set(
                    minX - CLIP_RECT_BUFFER, minY - CLIP_RECT_BUFFER,
                    (maxX - minX) + CLIP_RECT_BUFFER * 2, (maxY - minY) + CLIP_RECT_BUFFER * 2
                )

                this.renderChildren(
                    originX + childOriginX, originY - childOriginY, batch,
                    newClipRect, newUIOriginX, newUIOriginY
                )

                RectangleStack.pop()
            } else {
                this.renderChildren(
                    originX + childOriginX, originY - childOriginY, batch,
                    currentClipRect, newUIOriginX, newUIOriginY
                )
            }

            this.renderSelfAfterChildren(originX, originY, batch)

            val borderStyle = this.borderStyle.getOrCompute()
            if (shouldRenderBorder(borderStyle)) {
                borderStyle.renderBorder(originX, originY, batch, this)
            }
        }
    }

    open fun shouldRenderBorder(borderStyle: Border): Boolean {
        return true
    }

    protected open fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
    }

    private fun renderChildren(
        originX: Float, originY: Float, batch: SpriteBatch,
        currentClipRect: Rectangle, uiOriginX: Float, uiOriginY: Float,
    ) {
        val children = this.children.getOrCompute()
        if (children.isEmpty()) return

        val tmpRect = RectangleStack.getAndPush()
        for (child in children) {
            val childX = uiOriginX + child.bounds.x.get()
            val childY = uiOriginY + child.bounds.y.get()
            tmpRect.set(childX, childY, child.bounds.width.get(), child.bounds.height.get())
            if (!tmpRect.intersects(currentClipRect)) {
                continue
            }
            child.render(originX, originY, batch, currentClipRect, uiOriginX, uiOriginY)
        }
        RectangleStack.pop()
    }

    protected open fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
    }

    /**
     * Adds the [child] at the given [atIndex] of this [UIElement]'s children list.
     */
    fun addChild(atIndex: Int, child: UIElement): Boolean {
        val children = this._children.getOrCompute()
        if (child !in children) {
            child.parent.getOrCompute()?.removeChild(child)

            val childrenCopy = ArrayList<UIElement>(children.size + 1)
            childrenCopy.addAll(children)
            childrenCopy.add(atIndex, child)
            this._children.set(childrenCopy)
            child.parent.set(this)
            this.onChildAdded(child, atIndex)
            child.onAddedToParent(this)

            return true
        }
        return false
    }

    /**
     * Adds the [child] to the end of this [UIElement]'s children list.
     */
    fun addChild(child: UIElement): Boolean {
        return addChild(children.getOrCompute().size, child)
    }

    /**
     * Adds the [child] to the beginning of this [UIElement]'s children list.
     */
    fun addChildToBeginning(child: UIElement): Boolean {
        return addChild(0, child)
    }

    /**
     * Removes the given [child] from this [UIElement].
     */
    fun removeChild(child: UIElement): Boolean {
        return removeChild(children.getOrCompute().indexOf(child))
    }

    /**
     * Removes the child at the given [index] from this [UIElement].
     * If the [index] is not in bounds, no action is taken.
     */
    fun removeChild(index: Int): Boolean {
        val children = this._children.getOrCompute()
        if (index !in children.indices) return false

        val child = children[index]
        if (child is Focusable) {
            // Remove focus on this child.
            val childSceneRoot = child.sceneRoot.getOrCompute()
            childSceneRoot?.setFocusedElement(null)
        }

        this._children.set(children.toMutableList().apply {
            removeAt(index)
        })
        child.parent.set(null)
        this.onChildRemoved(child, index)
        child.onRemovedFromParent(this)

        return true
    }

    fun removeAllChildren() {
        val childrenSize = children.getOrCompute().size
        if (childrenSize > 0) {
            // Delete backwards
            for (i in childrenSize - 1 downTo 0) {
                removeChild(i)
            }
        }
    }

    /**
     * Called when a child is added to this [UIElement]. This will be called BEFORE the companion call to [onAddedToParent].
     */
    protected open fun onChildAdded(newChild: UIElement, atIndex: Int) {
    }

    /**
     * Called when a child is removed from this [UIElement]. This will be called BEFORE the companion call to [onRemovedFromParent].
     */
    protected open fun onChildRemoved(oldChild: UIElement, oldIndex: Int) {
    }

    /**
     * Called when this [UIElement] is added to a parent. This will be called AFTER the companion call to [onChildAdded].
     */
    protected open fun onAddedToParent(newParent: UIElement) {
    }

    /**
     * Called when this [UIElement] is removed from a parent. This will be called AFTER the companion call to [onChildRemoved].
     */
    protected open fun onRemovedFromParent(oldParent: UIElement) {
    }

    operator fun contains(other: UIElement): Boolean {
        return other in children.getOrCompute()
    }

    override fun toString(): String {
        return "{${this.javaClass.name}, x=${this.bounds.x.get()}, y=${this.bounds.y.get()}, w=${this.bounds.width.get()}, h=${this.bounds.height.get()}}"
    }

    fun addInputEventListener(listener: InputEventListener) {
        val current = inputListeners.getOrCompute()
        if (listener !in current) {
            inputListeners.set(current + listener)
        }
    }

    fun removeInputEventListener(listener: InputEventListener) {
        val current = inputListeners.getOrCompute()
        if (listener in current) {
            inputListeners.set(current - listener)
        }
    }

    /**
     * If this function returns true, this element is excluded from input pathing ([pathToForInput]).
     * This should not change its return value throughout the life of the element -- unexpected results may occur
     * due to how pathing works in the input system.
     */
    open fun shouldExcludeFromInput(): Boolean = false

    open fun sizeWidthToChildren(minimumWidth: Float = 0f, maximumWidth: Float = Float.POSITIVE_INFINITY): Float {
        // Use the farthest-right (X+) child based on right edge
        val last = children.getOrCompute().maxByOrNull { child ->
            child.bounds.x.get() + child.bounds.width.get()
        }
        var width = 0f
        if (last != null) {
            width = last.bounds.x.get() + last.bounds.width.get()
        }

        val borderInsets = this.border.getOrCompute()
        val marginInsets = this.margin.getOrCompute()
        val paddingInsets = this.padding.getOrCompute()

        width += borderInsets.leftAndRight() + marginInsets.leftAndRight() + paddingInsets.leftAndRight()

        val computedWidth = width.coerceIn(minimumWidth, maximumWidth)
        this.bounds.width.set(computedWidth)
        return computedWidth
    }

    open fun sizeHeightToChildren(minimumHeight: Float = 0f, maximumHeight: Float = Float.POSITIVE_INFINITY): Float {
        // Use the farthest-down (Y+) child based on bottom edge
        val last = children.getOrCompute().maxByOrNull { child ->
            child.bounds.y.get() + child.bounds.height.get()
        }
        var height = 0f
        if (last != null) {
            height = last.bounds.y.get() + last.bounds.height.get()
        }

        val borderInsets = this.border.getOrCompute()
        val marginInsets = this.margin.getOrCompute()
        val paddingInsets = this.padding.getOrCompute()

        height += borderInsets.topAndBottom() + marginInsets.topAndBottom() + paddingInsets.topAndBottom()

        val computedHeight = height.coerceIn(minimumHeight, maximumHeight)
        this.bounds.height.set(computedHeight)
        return computedHeight
    }

    /**
     * Begins clipping, defaulting to this UIElement's bounds. Wrap the drawing for the clip section in an
     * if statement with the return value of this function. Returns false if the resultant scissor would have zero area.
     * Call [SpriteBatch.flush] before calling this function and before calling [clipEnd].
     */
    fun clipBegin(originX: Float, originY: Float, x: Float, y: Float, width: Float, height: Float): Boolean {
        val root = sceneRoot.getOrCompute()
        val viewport = root?.viewport
        val rootBounds = root?.bounds
        val rootWidth: Float = rootBounds?.width?.get() ?: width
        val rootHeight: Float = rootBounds?.height?.get() ?: height

        val camWidth = (viewport?.screenWidth
            ?: Gdx.graphics.width).toFloat() //sceneRoot.getOrCompute()?.bounds?.width?.get() ?: Gdx.graphics.width.toFloat()
        val camHeight = (viewport?.screenHeight
            ?: Gdx.graphics.height).toFloat() //sceneRoot.getOrCompute()?.bounds?.height?.get() ?: Gdx.graphics.height.toFloat()

        val scissorX = (originX + x) / rootWidth * camWidth
        val scissorY = ((originY - y) / rootHeight) * camHeight
        val scissorW = (width / rootWidth) * camWidth
        val scissorH = (height / rootHeight) * camHeight
        val scissor = RectangleStack.getAndPush().set(scissorX, scissorY - scissorH, scissorW, scissorH)

        val pushScissor = if (root?.applyViewport?.get() == true)
            ScissorStack.pushScissor(scissor, viewport?.screenX ?: 0, viewport?.screenY ?: 0)
        else ScissorStack.pushScissor(scissor, 0, 0)
        return pushScissor
    }

    fun clipBegin(originX: Float, originY: Float): Boolean {
        val bounds = this.bounds
        return clipBegin(
            originX, originY, bounds.x.get(), bounds.y.get(),
            bounds.width.get(), bounds.height.get()
        )
    }

    fun clipEnd() {
        val rect = ScissorStack.popScissor()
        if (rect != null) {
            RectangleStack.pop()
        }
    }


    @Suppress("SimpleRedundantLet")
    fun bindVarToParentWidth(varr: FloatVar, adjust: Float = 0f, multiplier: Float = 1f) {
        varr.bind {
            (this@UIElement.parent.use()?.let { p -> p.contentZone.width.use() } ?: 0f) * multiplier + adjust
        }
    }

    @Suppress("SimpleRedundantLet")
    fun bindVarToParentHeight(varr: FloatVar, adjust: Float = 0f, multiplier: Float = 1f) {
        varr.bind {
            (this@UIElement.parent.use()?.let { p -> p.contentZone.height.use() } ?: 0f) * multiplier + adjust
        }
    }

    @Suppress("SimpleRedundantLet")
    fun bindVarToParentWidth(
        varr: FloatVar,
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        varr.bind {
            (this@UIElement.parent.use()?.let { p -> p.contentZone.width.use() }
                ?: 0f) * multiplierBinding() + adjustBinding()
        }
    }

    @Suppress("SimpleRedundantLet")
    fun bindVarToParentHeight(
        varr: FloatVar,
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        varr.bind {
            (this@UIElement.parent.use()?.let { p -> p.contentZone.height.use() }
                ?: 0f) * multiplierBinding() + adjustBinding()
        }
    }

    fun bindVarToSelfHeight(varr: FloatVar, adjust: Float = 0f, multiplier: Float = 1f) {
        val thisBounds = this.bounds
        varr.bind {
            thisBounds.height.use() * multiplier + adjust
        }
    }

    fun bindVarToSelfWidth(varr: FloatVar, adjust: Float = 0f, multiplier: Float = 1f) {
        val thisBounds = this.bounds
        varr.bind {
            thisBounds.width.use() * multiplier + adjust
        }
    }

    fun bindVarToSelfHeight(
        varr: FloatVar,
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        val thisBounds = this.bounds
        varr.bind {
            thisBounds.height.use() * multiplierBinding() + adjustBinding()
        }
    }

    fun bindVarToSelfWidth(
        varr: FloatVar,
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        val thisBounds = this.bounds
        varr.bind {
            thisBounds.width.use() * multiplierBinding() + adjustBinding()
        }
    }


    fun bindWidthToParent(adjust: Float = 0f, multiplier: Float = 1f) {
        bindVarToParentWidth(this.bounds.width, adjust, multiplier)
    }

    fun bindHeightToParent(adjust: Float = 0f, multiplier: Float = 1f) {
        bindVarToParentHeight(this.bounds.height, adjust, multiplier)
    }

    fun bindWidthToParent(
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        bindVarToParentWidth(this.bounds.width, multiplierBinding, adjustBinding)
    }

    fun bindHeightToParent(
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        bindVarToParentHeight(this.bounds.height, multiplierBinding, adjustBinding)
    }

    fun bindWidthToSelfHeight(adjust: Float = 0f, multiplier: Float = 1f) {
        bindVarToSelfHeight(this.bounds.width, adjust, multiplier)
    }

    fun bindHeightToSelfWidth(adjust: Float = 0f, multiplier: Float = 1f) {
        bindVarToSelfWidth(this.bounds.height, adjust, multiplier)
    }

    fun bindWidthToSelfHeight(
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        bindVarToSelfHeight(this.bounds.width, multiplierBinding, adjustBinding)
    }

    fun bindHeightToSelfWidth(
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        bindVarToSelfWidth(this.bounds.height, multiplierBinding, adjustBinding)
    }

    fun bindXToParentWidth(adjust: Float = 0f, multiplier: Float = 1f) {
        bindVarToParentWidth(this.bounds.x, adjust, multiplier)
    }

    fun bindYToParentHeight(adjust: Float = 0f, multiplier: Float = 1f) {
        bindVarToParentHeight(this.bounds.y, adjust, multiplier)
    }

    fun bindXToParentWidth(
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        bindVarToParentWidth(this.bounds.x, multiplierBinding, adjustBinding)
    }

    fun bindYToParentHeight(
        multiplierBinding: ContextBinding<Float> = DEFAULT_MULTIPLIER_BINDING,
        adjustBinding: ContextBinding<Float>,
    ) {
        bindVarToParentHeight(this.bounds.y, multiplierBinding, adjustBinding)
    }

    protected inline fun renderOptionallyWithClip(
        originX: Float, originY: Float, batch: SpriteBatch, clip: Boolean,
        renderFunc: (originX: Float, originY: Float, batch: SpriteBatch) -> Unit,
    ) {
        if (clip) {
            batch.flush()
            if (clipBegin(originX, originY)) {
                renderFunc(originX, originY, batch)
                batch.flush()
                clipEnd()
            }
        } else {
            renderFunc(originX, originY, batch)
        }
    }

    /**
     * Returns a list of UIElements from this element to the child that contains the point.
     * IMPLEMENTATION NOTE: This function assumes that all the children for a parent fit inside of that parent's bounds.
     */
    fun pathTo(x: Float, y: Float): List<UIElement> {
        val res = mutableListOf<UIElement>()
        var current: UIElement = this
        var currentChildren: List<UIElement> = current.children.getOrCompute()
        var currentBounds: ReadOnlyBounds = current.contentZone
        var xOffset: Float = currentBounds.x.get()
        var yOffset: Float = currentBounds.y.get()
        while (currentChildren.isNotEmpty()) {
            val found = currentChildren.findLast { child ->
                child.bounds.containsPointLocal(x - xOffset, y - yOffset)
            } ?: break
            res += found
            current = found
            currentChildren = current.children.getOrCompute()
            currentBounds = current.contentZone
            xOffset += currentBounds.x.get()
            yOffset += currentBounds.y.get()
        }
        return res
    }

    /**
     * Returns a list of [UIElement]s from this element to the child that contains the point within [UIBounds.contentZone].
     * It also excludes elements that are not [visible] or whose [shouldExcludeFromInput] return true.
     * IMPLEMENTATION NOTE: This function assumes that all the children for a parent fit inside of that parent's bounds.
     */
    fun pathToForInput(x: Float, y: Float): List<UIElement> {
        val res = mutableListOf<UIElement>()
        var current: UIElement = this
        var currentChildren: List<UIElement> = current.children.getOrCompute()
        var currentBounds: ReadOnlyBounds = current.contentZone
        var xOffset: Float = currentBounds.x.get()
        var yOffset: Float = currentBounds.y.get()
        while (currentChildren.isNotEmpty()) {
            /*
            Clipping check:
            If current is clipped, then x and y MUST be within current to begin with!
             */
            if (current.doClipping.get()) {
                if (!current.bounds.containsPointLocal(x, y)) break
            }
            val found = currentChildren.findLast { child ->
                child.apparentVisibility.get() && !child.shouldExcludeFromInput()
                        && child.borderZone.containsPointLocal(x - xOffset, y - yOffset)
            } ?: break

            res += found
            current = found
            currentChildren = current.children.getOrCompute()
            currentBounds = current.contentZone
            xOffset += currentBounds.x.get()
            yOffset += currentBounds.y.get()
        }
        return res
    }

    /**
     * Returns the xy position relative to the uppermost parent.
     */
    fun getPosRelativeToRoot(vector: Vector2 = Vector2(0f, 0f)): Vector2 {
        vector.set(0f, 0f)
        // Traverse up the tree
        var current: UIElement = this
        var currentParent: UIElement? = current.parent.getOrCompute()
        while (currentParent != null) {
            val bounds: ReadOnlyBounds = currentParent.contentZone
            vector.x += bounds.x.get()
            vector.y += bounds.y.get()
            current = currentParent
            currentParent = current.parent.getOrCompute()
        }

        vector.x += this.bounds.x.get()
        vector.y += this.bounds.y.get()

        return vector
    }

    operator fun plusAssign(child: UIElement) {
        addChild(child)
    }

    operator fun minusAssign(child: UIElement) {
        removeChild(child)
    }

}

