package paintbox.ui.control

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar
import paintbox.binding.Var
import paintbox.ui.Anchor
import paintbox.ui.Pane
import paintbox.ui.Scrolled
import paintbox.ui.UIElement
import paintbox.ui.skin.DefaultSkins
import paintbox.ui.skin.Skin
import paintbox.ui.skin.SkinFactory
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect
import paintbox.util.gdxutils.isAltDown
import paintbox.util.gdxutils.isControlDown
import paintbox.util.gdxutils.isShiftDown


/**
 * A [ScrollPane] has content that is panned around. It also has optional [ScrollBar]s for user interactivity.
 */
open class ScrollPane : Control<ScrollPane>() {

    companion object {

        const val SCROLLPANE_SKIN_ID: String = "ScrollPane"

        init {
            DefaultSkins.register(ScrollPane.SCROLLPANE_SKIN_ID, SkinFactory { element: ScrollPane ->
                ScrollPaneSkin(element)
            })
        }
    }

    enum class ScrollBarPolicy {
        NEVER, ALWAYS, AS_NEEDED
    }

    val hBar: ScrollBar by lazy { createScrollBar(ScrollBar.Orientation.HORIZONTAL) }
    val vBar: ScrollBar by lazy { createScrollBar(ScrollBar.Orientation.VERTICAL) }
    val contentPane: Pane = ContentPane().apply {
        this.doClipping.set(true)
    }
    private val currentContent: Var<UIElement?> = Var(null)

    val hBarPolicy: Var<ScrollBarPolicy> = Var(ScrollBarPolicy.AS_NEEDED)
    val vBarPolicy: Var<ScrollBarPolicy> = Var(ScrollBarPolicy.AS_NEEDED)
    val barSize: FloatVar = FloatVar(15f)
    val minThumbSize: FloatVar = FloatVar(20f)

    /**
     * The current width of the content UI element.
     */
    val currentW: ReadOnlyFloatVar = FloatVar(eager = true) {
        currentContent.use()?.bounds?.width?.use() ?: 0f
    }

    /**
     * The current height of the content UI element.
     */
    val currentH: ReadOnlyFloatVar = FloatVar(eager = true) {
        currentContent.use()?.bounds?.height?.use() ?: 0f
    }

    /**
     * The current height of the content pane (visible area).
     */
    val contentPaneWidth: ReadOnlyFloatVar = FloatVar(eager = true) {
        contentPane.contentZone.width.use()
    }

    /**
     * The current height of the content pane (visible area).
     */
    val contentPaneHeight: ReadOnlyFloatVar = FloatVar(eager = true) {
        contentPane.contentZone.height.use()
    }

    /**
     * The difference between the width of the content and the width of the visible area.
     * Effectively this is how much you can scroll horizontally.
     */
    val contentWidthDiff: ReadOnlyFloatVar = FloatVar(eager = true) {
        currentW.use() - contentPaneWidth.use()
    }

    /**
     * The difference between the height of the content and the height of the visible area.
     * Effectively this is how much you can scroll vertically.
     */
    val contentHeightDiff: ReadOnlyFloatVar = FloatVar(eager = true) {
        currentH.use() - contentPaneHeight.use()
    }

    init {
        hBar.bounds.height.bind(barSize)
        hBar.bindWidthToParent {
            if (vBar.visible.use()) (-barSize.use()) else 0f
        }
        Anchor.BottomLeft.configure(hBar)
        vBar.bounds.width.bind(barSize)
        vBar.bindHeightToParent {
            if (hBar.visible.use()) (-barSize.use()) else 0f
        }
        Anchor.TopRight.configure(vBar)
        Anchor.TopLeft.configure(contentPane)
        contentPane.bindWidthToParent {
            // When the scrollbar policy is AS_NEEDED, there is an inf loop due to depending on visibility which depends on contentPane bounds
//            if (vBar.apparentVisibility.use()) (-barSize.use()) else 0f
            val policy = vBarPolicy.use()
            if (policy == ScrollBarPolicy.NEVER) 0f else (-barSize.use())
        }
        contentPane.bindHeightToParent {
//            if (hBar.apparentVisibility.use()) (-barSize.use()) else 0f
            val policy = hBarPolicy.use()
            if (policy == ScrollBarPolicy.NEVER) 0f else (-barSize.use())
        }
        contentPane.contentOffsetX.eagerBind { -hBar.value.use() }
        contentPane.contentOffsetY.eagerBind { -vBar.value.use() }

        hBar.visible.bind {
            when (hBarPolicy.getOrCompute()) {
                ScrollBarPolicy.NEVER -> false
                ScrollBarPolicy.ALWAYS -> true
                ScrollBarPolicy.AS_NEEDED -> contentWidthDiff.use() > 0f
            }
        }
        vBar.visible.bind {
            when (vBarPolicy.getOrCompute()) {
                ScrollBarPolicy.NEVER -> false
                ScrollBarPolicy.ALWAYS -> true
                ScrollBarPolicy.AS_NEEDED -> contentHeightDiff.use() > 0f
            }
        }
        hBar.minimum.set(0f)
        vBar.minimum.set(0f)
        hBar.maximum.eagerBind { contentWidthDiff.use().coerceAtLeast(0f) }
        vBar.maximum.eagerBind { contentHeightDiff.use().coerceAtLeast(0f) }
        hBar.visibleAmount.eagerBind {
            val barMax = hBar.maximum.use()
            ((contentPaneWidth.use() / currentW.use()) * barMax)
                .coerceAtMost(barMax)
                .coerceAtLeast(minThumbSize.use())
        }
        vBar.visibleAmount.eagerBind {
            val barMax = vBar.maximum.use()
            ((contentPaneHeight.use() / currentH.use()) * barMax)
                .coerceAtMost(barMax)
                .coerceAtLeast(minThumbSize.use())
        }

        addChild(contentPane)
        addChild(hBar)
        addChild(vBar)

        this.addInputEventListener { event ->
            if (event is Scrolled && !Gdx.input.isControlDown() && !Gdx.input.isAltDown()) {
                val shift = Gdx.input.isShiftDown()
                val vBarAmount = if (shift) event.amountX else event.amountY
                val hBarAmount = if (shift) event.amountY else event.amountX

                if (vBarAmount != 0f && vBar.apparentVisibility.get() && !vBar.apparentDisabledState.get()) {
                    if (vBarAmount > 0) vBar.incrementBlock() else vBar.decrementBlock()
                }
                if (hBarAmount != 0f && hBar.apparentVisibility.get() && !hBar.apparentDisabledState.get()) {
                    if (hBarAmount > 0) hBar.incrementBlock() else hBar.decrementBlock()
                }
            }
            false
        }
    }

    protected open fun createScrollBar(orientation: ScrollBar.Orientation): ScrollBar {
        return ScrollBar(orientation)
    }

    fun setContent(element: UIElement) {
        val lastCurrent = this.currentContent.getOrCompute()
        if (lastCurrent != null) {
            this.currentContent.set(null)
//            lastCurrent.bounds.width.removeListener(contentWHListener)
//            lastCurrent.bounds.height.removeListener(contentWHListener)
            contentPane.removeChild(lastCurrent)
        }
        this.currentContent.set(element)
        element.bounds.x.set(0f)
        element.bounds.y.set(0f)
//        element.bounds.width.addListener(contentWHListener)
//        element.bounds.height.addListener(contentWHListener)
        contentPane.addChild(element)
    }

    fun getContent(): UIElement? = currentContent.getOrCompute()

    override fun getDefaultSkinID(): String = ScrollPane.SCROLLPANE_SKIN_ID

    class ContentPane : Pane()
}

open class ScrollPaneSkin(element: ScrollPane) : Skin<ScrollPane>(element) {

    val bgColor: Var<Color> = Var(Color(0.94f, 0.94f, 0.94f, 0f))

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val contentBounds = element.contentZone
        val rectX = contentBounds.x.get() + originX
        val rectY = originY - contentBounds.y.get()
        val rectW = contentBounds.width.get()
        val rectH = contentBounds.height.get()
        val lastPackedColor = batch.packedColor
        val opacity = element.apparentOpacity.get()
        val tmpColor = ColorStack.getAndPush()

        tmpColor.set(bgColor.getOrCompute())
        tmpColor.a *= opacity

        batch.color = tmpColor
        batch.fillRect(rectX, rectY - rectH, rectW, rectH)

        batch.packedColor = lastPackedColor
        ColorStack.pop()
    }

    override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
    }
}