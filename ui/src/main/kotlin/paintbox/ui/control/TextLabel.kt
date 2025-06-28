package paintbox.ui.control

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import paintbox.binding.*
import paintbox.font.*
import paintbox.ui.UIElement
import paintbox.ui.area.Insets
import paintbox.ui.border.Border
import paintbox.ui.skin.DefaultSkins
import paintbox.ui.skin.Skin
import paintbox.ui.skin.SkinFactory
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect
import kotlin.math.min


/**
 * A [TextLabel] is a [Control] that renders a [TextBlock]
 */
open class TextLabel(text: String, font: PaintboxFont = UIElement.defaultFont) : Control<TextLabel>(),
    HasLabelComponent {

    companion object {

        const val TEXTLABEL_SKIN_ID: String = "TextLabel"
        const val SCROLLING_TEXTLABEL_SKIN_ID: String = "TextLabel_scrolling"

        init {
            DefaultSkins.register(TEXTLABEL_SKIN_ID, SkinFactory { element: TextLabel ->
                TextLabelSkin(element)
            })
            DefaultSkins.register(SCROLLING_TEXTLABEL_SKIN_ID, SkinFactory { element: TextLabel ->
                ScrollingTextLabelSkin(element)
            })
        }

        fun createInternalTextBlockVar(label: TextLabel): Var<TextBlock> {
            return Var {
                val markup: Markup? = label.markup.use()
                (markup?.parse(label.text.use())
                    ?: TextRun(
                        label.font.use(), label.text.use(), Color.WHITE, 1f, 1f,
                        lineHeightScale = label.lineSpacingMultiplier.use()
                    ).toTextBlock()).also { textBlock ->
                    if (label.doLineWrapping.use()) {
                        textBlock.lineWrapping.set(label.contentZone.width.use() / (if (label.doesScaleXAffectWrapping.use()) label.scaleX.use() else 1f))
                    }
                }
            }
        }
    }

    /**
     * The autosizing behaviour.
     */
    sealed class AutosizeBehavior {

        data object None : AutosizeBehavior()

        /**
         * Autosizing is active.
         *
         * [dimensions] indicates whether width, height, or both should be affected.
         *
         * [triggerOnlyWhenInScene] is true by default; a value of true means the autosize will only happen
         * if the [TextLabel.sceneRoot] is not null. This works because [TextLabel.internalTextBlock]
         * is accessed when rendering (and thus updated), and the label must be in the scene graph to be rendered.
         */
        class Active(val dimensions: Dimensions, val triggerOnlyWhenInScene: Boolean = true) : AutosizeBehavior()

        enum class Dimensions(val affectWidth: Boolean, val affectHeight: Boolean) {
            WIDTH_ONLY(true, false),
            HEIGHT_ONLY(false, true),
            WIDTH_AND_HEIGHT(true, true),
        }
    }

    override val text: Var<String> = Var(text)
    override val font: Var<PaintboxFont> = Var(font)

    /**
     * The [Markup] object to use. If null, no markup parsing is done. If not null,
     * then the markup determines the TextBlock (and other values like [textColor] are ignored).
     */
    override val markup: Var<Markup?> = Var(null)

    /**
     * If the alpha value is 0, the skin controls what text colour is used.
     */
    val textColor: Var<Color> = Var(Color(0f, 0f, 0f, 0f))

    /**
     * Determines the x-scale the text is rendered at.
     */
    override val scaleX: FloatVar = FloatVar(1f)

    /**
     * Determines the y-scale the text is rendered at.
     */
    override val scaleY: FloatVar = FloatVar(1f)

    override val lineSpacingMultiplier: FloatVar = FloatVar(1f)

    val doesScaleXAffectWrapping: BooleanVar = BooleanVar(true)

    /**
     * If the alpha value is 0, the skin controls what background colour is used.
     */
    val backgroundColor: Var<Color> = Var(Color(1f, 1f, 1f, 0f))

    val renderBackground: BooleanVar = BooleanVar(false)
    val bgPadding: Var<Insets> = Var.bind { padding.use() }

    val renderAlign: IntVar = IntVar(Align.left)
    val textAlign: Var<TextAlign> = Var { TextAlign.fromInt(renderAlign.use()) }
    val doXCompression: BooleanVar = BooleanVar(true)
    val doLineWrapping: BooleanVar = BooleanVar(false)
    val autosizeBehavior: Var<AutosizeBehavior> = Var(AutosizeBehavior.None)

    /**
     * The maximum width of this label when auto-resized. A value of zero means there is no limit.
     */
    val maxWidth: FloatVar = FloatVar(0f)

    /**
     * The maximum height of this label when auto-resized. A value of zero means there is no limit.
     */
    val maxHeight: FloatVar = FloatVar(0f)

    /**
     * Defaults to an auto-generated [TextBlock] with the given [text].
     *
     * If this is overwritten, this [TextLabel]'s [textColor] should be set to have a non-zero opacity.
     */
    val internalTextBlock: Var<TextBlock> = createInternalTextBlockVar(this)

    constructor(binding: ContextBinding<String>, font: PaintboxFont = UIElement.defaultFont)
            : this("", font) {
        text.bind(binding)
    }

    constructor(bindable: ReadOnlyVar<String>, font: PaintboxFont = UIElement.defaultFont)
            : this("", font) {
        text.bind(bindable)
    }

    init {
        val autosizeListener = VarChangedListener<Any?> {
            triggerAutosize()
        }
        this.autosizeBehavior.addListener(autosizeListener)
        this.internalTextBlock.addListener(autosizeListener)
        this.maxWidth.addListener(autosizeListener)
        this.maxHeight.addListener(autosizeListener)
        this.sceneRoot.addListener {
            val behaviour = autosizeBehavior.getOrCompute()
            if (behaviour is AutosizeBehavior.Active && behaviour.triggerOnlyWhenInScene) {
                triggerAutosize()
            }
        }
    }

    override fun shouldRenderBorder(borderStyle: Border): Boolean {
        val text = this.internalTextBlock.getOrCompute()
        return text.runs.isNotEmpty()
    }

    fun triggerAutosize() {
        when (val b = autosizeBehavior.getOrCompute()) {
            AutosizeBehavior.None -> {}
            is AutosizeBehavior.Active -> {
                if (!b.triggerOnlyWhenInScene || this.sceneRoot.getOrCompute() != null) {
                    resizeBoundsToContent(
                        limitWidth = maxWidth.get(), limitHeight = maxHeight.get(),
                        affectWidth = b.dimensions.affectWidth, affectHeight = b.dimensions.affectHeight
                    )
                }
            }
        }
    }

    @Suppress("RemoveRedundantQualifierName")
    override fun getDefaultSkinID(): String = TextLabel.TEXTLABEL_SKIN_ID

    /**
     * Resizes this element to fit the bounds of the [internalTextBlock].
     * [limitWidth] and [limitHeight] will strictly limit the width/height if they are greater than zero.
     *
     */
    fun resizeBoundsToContent(
        affectWidth: Boolean = true, affectHeight: Boolean = true,
        limitWidth: Float = 0f, limitHeight: Float = 0f,
    ) {
        val textBlock: TextBlock = this.internalTextBlock.getOrCompute()
        textBlock.computeLayoutsIfNeeded()
        if (!affectWidth && !affectHeight) return

        val textWidth = textBlock.width * scaleX.get()
        val textHeight = textBlock.height * scaleY.get()

        val borderInsets = this.border.getOrCompute()
        val marginInsets = this.margin.getOrCompute()
        val paddingInsets = this.bgPadding.getOrCompute().maximize(this.padding.getOrCompute())

        fun Insets.leftright(): Float = this.left + this.right
        fun Insets.topbottom(): Float = this.top + this.bottom

        if (affectWidth) {
            var computedWidth =
                borderInsets.leftright() + marginInsets.leftright() + paddingInsets.leftright() + textWidth
            if (limitWidth > 0f) {
                computedWidth = computedWidth.coerceAtMost(limitWidth)
            }
            this.bounds.width.set(computedWidth)
        }
        if (affectHeight) {
            var computedHeight =
                borderInsets.topbottom() + marginInsets.topbottom() + paddingInsets.topbottom() + textHeight
            if (limitHeight > 0f) {
                computedHeight = computedHeight.coerceAtMost(limitHeight)
            }
            this.bounds.height.set(computedHeight)
        }
    }
}

open class TextLabelSkin(element: TextLabel) : Skin<TextLabel>(element) {

    val defaultTextColor: Var<Color> = Var(Color(0f, 0f, 0f, 1f))
    val defaultBgColor: Var<Color> = Var(Color(1f, 1f, 1f, 1f))

    protected val textColorToUse: ReadOnlyVar<Color> = Var {
        val color = element.textColor.use()
        if (color.a <= 0f) {
            // Use the skin's default colour.
            defaultTextColor.use()
        } else {
            element.textColor.use()
        }
    }
    protected val bgColorToUse: ReadOnlyVar<Color> = Var {
        val color = element.backgroundColor.use()
        if (color.a <= 0f) {
            // Use the skin's default colour.
            defaultBgColor.use()
        } else {
            element.backgroundColor.use()
        }
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val text = element.internalTextBlock.getOrCompute()
        if (text.runs.isEmpty()) return

        val bounds = element.contentZone
        val x = bounds.x.get() + originX
        val y = originY - bounds.y.get()
        val w = bounds.width.get()
        val h = bounds.height.get()
        val lastPackedColor = batch.packedColor
        val opacity = element.apparentOpacity.get()
        val tmpColor = ColorStack.getAndPush()
        tmpColor.set(batch.color).mul(textColorToUse.getOrCompute())
        tmpColor.a *= opacity

        text.computeLayoutsIfNeeded()

        val bgPaddingInsets = if (element.renderBackground.get()) element.bgPadding.getOrCompute() else Insets.ZERO
        val compressX = element.doXCompression.get()
        val align = element.renderAlign.get()
        val scaleX = element.scaleX.get()
        val scaleY = element.scaleY.get()
        val textWidth = text.width * scaleX
        val textHeight = text.height * scaleY
        val xOffset: Float = when {
            Align.isLeft(align) -> 0f + bgPaddingInsets.left
            Align.isRight(align) -> (w - ((if (compressX) (min(textWidth, w)) else textWidth) + bgPaddingInsets.right))
            else -> (w - (if (compressX) min(textWidth, w) else textWidth)) / 2f
        }
        val firstCapHeight = text.firstCapHeight * scaleY
        val yOffset: Float = when {
            Align.isTop(align) -> h - firstCapHeight - bgPaddingInsets.top
            Align.isBottom(align) -> 0f + (textHeight - firstCapHeight) + bgPaddingInsets.bottom
            else -> ((h + textHeight) / 2 - firstCapHeight)
        }

        if (element.renderBackground.get()) {
            // Draw a rectangle behind the text, only sizing to the text area.
            val bx = (x + xOffset) - bgPaddingInsets.left
            val by = (y - h + yOffset - textHeight + firstCapHeight) - bgPaddingInsets.top
            val bw = (if (compressX) min(w, textWidth) else textWidth) + bgPaddingInsets.left + bgPaddingInsets.right
            val bh = textHeight + bgPaddingInsets.top + bgPaddingInsets.bottom

            val bgColor = ColorStack.getAndPush().set(bgColorToUse.getOrCompute())
            bgColor.a *= opacity
            batch.color = bgColor
            batch.fillRect(
                bx.coerceAtLeast(x), by/*.coerceAtLeast(y - bh.coerceAtMost(h))*/,
                if (compressX) bw.coerceAtMost(w) else bw, bh/*.coerceAtMost(h)*/
            )
            ColorStack.pop()
        }

        batch.color = tmpColor // Sets the opacity of the text
        if (compressX) {
            val maxTextWidth = w - bgPaddingInsets.left - bgPaddingInsets.right
            text.drawCompressed(
                batch, x + (xOffset).coerceAtLeast(bgPaddingInsets.left), (y - h + yOffset),
                maxTextWidth,
                element.textAlign.getOrCompute(), scaleX, scaleY
            )
        } else {
            text.draw(batch, x + xOffset, (y - h + yOffset), element.textAlign.getOrCompute(), scaleX, scaleY)
        }
        ColorStack.pop()

        batch.packedColor = lastPackedColor
    }

    override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        // NO-OP
    }

}

/**
 * A text label that scrolls and wraps its text like a music player would.
 * [doClipping][paintbox.ui.UIElement.doClipping] should be enabled for the best effect
 */
class ScrollingTextLabelSkin(element: TextLabel) : TextLabelSkin(element) {

    val scrollRate: FloatVar = FloatVar(64f)
    val wrapAroundPauseSec: FloatVar = FloatVar(2f)
    val gapBetween: FloatVar = FloatVar { scrollRate.use() }

    /**
     * The offset is positive.
     * It wraps around to 0 when the value is greater than or equal to
     * the [text block][TextLabel.internalTextBlock] width plus the content zone width.
     */
    private var currentScrollOffset: Float = 0f
    private var pauseTimer: Float = 0f

    init {
        element.internalTextBlock.addListener {
            reset()
        }
    }

    private fun reset() {
        currentScrollOffset = 0f
        pauseTimer = wrapAroundPauseSec.get()
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val text = element.internalTextBlock.getOrCompute()
        if (text.runs.isEmpty()) {
            return
        }

        val bounds = element.contentZone
        val x = bounds.x.get() + originX
        val y = originY - bounds.y.get()
        val w = bounds.width.get()
        val h = bounds.height.get()
        val lastPackedColor = batch.packedColor
        val opacity = element.apparentOpacity.get()
        val tmpColor = ColorStack.getAndPush()
        tmpColor.set(batch.color).mul(textColorToUse.getOrCompute())
        tmpColor.a *= opacity

        if (text.computeLayoutsIfNeeded()) {
            reset()
        }

        val bgPaddingInsets = if (element.renderBackground.get()) element.bgPadding.getOrCompute() else Insets.ZERO
        val compressX = element.doXCompression.get()
        val align = element.renderAlign.get()
        val scaleX = element.scaleX.get()
        val scaleY = element.scaleY.get()
        val textWidth = text.width * scaleX
        val textHeight = text.height * scaleY
        val textWrapPoint = textWidth + gapBetween.get() * scaleX
        val xOffset: Float = if (textWrapPoint > w) (0f + bgPaddingInsets.left) else when {
            Align.isLeft(align) -> 0f + bgPaddingInsets.left
            Align.isRight(align) -> (w - ((if (compressX) (min(textWidth, w)) else textWidth) + bgPaddingInsets.right))
            else -> (w - (if (compressX) min(textWidth, w) else textWidth)) / 2f
        }
        val firstCapHeight = text.firstCapHeight * scaleY
        val yOffset: Float = when {
            Align.isTop(align) -> h - firstCapHeight - bgPaddingInsets.top
            Align.isBottom(align) -> 0f + (textHeight - firstCapHeight) + bgPaddingInsets.bottom
            else -> ((h + textHeight) / 2 - firstCapHeight)
        }

        if (element.renderBackground.get()) {
            // Draw a rectangle behind the text, only sizing to the text area.
            val bx = (x + xOffset) - bgPaddingInsets.left
            val by = (y - h + yOffset - textHeight + firstCapHeight) - bgPaddingInsets.top
            val bw = (if (compressX) min(w, textWidth) else textWidth) + bgPaddingInsets.left + bgPaddingInsets.right
            val bh = textHeight + bgPaddingInsets.top + bgPaddingInsets.bottom

            val bgColor = ColorStack.getAndPush().set(bgColorToUse.getOrCompute())
            bgColor.a *= opacity
            batch.color = bgColor
            batch.fillRect(
                bx.coerceAtLeast(x), by/*.coerceAtLeast(y - bh.coerceAtMost(h))*/,
                if (compressX) bw.coerceAtMost(w) else bw, bh/*.coerceAtMost(h)*/
            )
            ColorStack.pop()
        }

        batch.color = tmpColor // Sets the opacity of the text
        val deltaTime = Gdx.graphics.deltaTime
        val scrollOffset: Float = if (textWrapPoint < w) 0f else if (pauseTimer > 0f) {
            pauseTimer = (pauseTimer - deltaTime).coerceAtLeast(0f)
            0f
        } else {
            val oldScrollOffset = this.currentScrollOffset
            val newScrollOffset = oldScrollOffset + this.scrollRate.get() * deltaTime
            this.currentScrollOffset = if (oldScrollOffset < textWrapPoint && newScrollOffset >= textWrapPoint) {
                pauseTimer = wrapAroundPauseSec.get()
                0f
            } else {
                newScrollOffset
            }
            -(this.currentScrollOffset)
        }
        if (compressX) {
            val maxTextWidth = w - bgPaddingInsets.left - bgPaddingInsets.right
            text.drawCompressed(
                batch, x + (xOffset).coerceAtLeast(bgPaddingInsets.left) + scrollOffset, (y - h + yOffset),
                maxTextWidth,
                element.textAlign.getOrCompute(), scaleX, scaleY
            )
            if (scrollOffset < 0f) {
                text.drawCompressed(
                    batch,
                    x + (xOffset).coerceAtLeast(bgPaddingInsets.left) + scrollOffset + textWrapPoint,
                    (y - h + yOffset),
                    maxTextWidth,
                    element.textAlign.getOrCompute(),
                    scaleX,
                    scaleY
                )
            }
        } else {
            text.draw(
                batch,
                x + xOffset + scrollOffset,
                (y - h + yOffset),
                element.textAlign.getOrCompute(),
                scaleX,
                scaleY
            )
            if (scrollOffset < 0f) {
                text.draw(
                    batch,
                    x + xOffset + scrollOffset + textWrapPoint,
                    (y - h + yOffset),
                    element.textAlign.getOrCompute(),
                    scaleX,
                    scaleY
                )
            }
        }
        ColorStack.pop()

        batch.packedColor = lastPackedColor
    }

    override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        // NO-OP
    }

}
