package paintbox.ui.control

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import paintbox.PaintboxGame
import paintbox.binding.*
import paintbox.font.*
import paintbox.ui.StringConverter
import paintbox.ui.UIElement
import paintbox.ui.area.Insets
import paintbox.ui.border.SolidBorder
import paintbox.ui.skin.DefaultSkins
import paintbox.ui.skin.Skin
import paintbox.ui.skin.SkinFactory
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect
import kotlin.math.min


open class ComboBox<T>(
    startingList: List<T>, selectedItem: T,
    font: PaintboxFont = UIElement.defaultFont,
) : Control<ComboBox<T>>(), HasLabelComponent, HasItemDropdown<T>, HasSelectedItem<T> {

    companion object {

        const val COMBOBOX_SKIN_ID: String = "ComboBox"

        val DEFAULT_STRING_CONVERTER: StringConverter<Any?> get() = StringConverter.DEFAULT_STRING_CONVERTER
        val DEFAULT_PADDING: Insets = Insets(2f, 2f, 4f, 4f)

        init {
            DefaultSkins.register(COMBOBOX_SKIN_ID, SkinFactory { element: ComboBox<*> ->
                @Suppress("UNCHECKED_CAST")
                ComboBoxSkin(element as ComboBox<Any?>)
            })
        }

        fun createInternalTextBlockVar(comboBox: ComboBox<Any?>): Var<TextBlock> {
            return Var {
                val text = comboBox.text.use()
                val markup: Markup? = comboBox.markup.use()
                markup?.parse(text)
                    ?: TextRun(
                        comboBox.font.use(), text, Color.WHITE,
                        comboBox.scaleX.use(), comboBox.scaleY.use(),
                        lineHeightScale = comboBox.lineSpacingMultiplier.use()
                    ).toTextBlock()
            }
        }
    }


    override val items: Var<List<T>> = Var(startingList)
    override val selectedItem: Var<T> = Var(selectedItem)

    @Suppress("UNCHECKED_CAST")
    override val itemStringConverter: Var<StringConverter<T>> = Var(DEFAULT_STRING_CONVERTER as StringConverter<T>)
    override val text: ReadOnlyVar<String> = Var.bind {
        this@ComboBox.itemStringConverter.use().convert(this@ComboBox.selectedItem.use())
    }

    val backgroundColor: Var<Color> = Var(Color(1f, 1f, 1f, 1f))
    val contrastColor: Var<Color> = Var(Color(0f, 0f, 0f, 1f))
    val textColor: Var<Color> = Var.bind { contrastColor.use() }
    val arrowColor: Var<Color> = Var.bind { contrastColor.use() }

    override val font: Var<PaintboxFont> = Var(font)
    override val scaleX: FloatVar = FloatVar(1f)
    override val scaleY: FloatVar = FloatVar(1f)
    override val lineSpacingMultiplier: FloatVar = FloatVar(1f)
    val renderAlign: IntVar = IntVar(Align.left)
    val textAlign: Var<TextAlign> = Var { TextAlign.fromInt(renderAlign.use()) }
    val doXCompression: BooleanVar = BooleanVar(true)

    /**
     * The [Markup] object to use. If null, no markup parsing is done. If not null,
     * then the markup determines the TextBlock (and other values like [textColor] are ignored).
     */
    override val markup: Var<Markup?> = Var(null)

    /**
     * Defaults to an auto-generated [TextBlock] for the given toString representation of the selected item.
     */
    val internalTextBlock: Var<TextBlock> by lazy {
        @Suppress("UNCHECKED_CAST")
        createInternalTextBlockVar(this as ComboBox<Any?>)
    }

    /**
     * Fired whenever an item was selected, even if it was already selected previously.
     */
    override var onItemSelected: (T) -> Unit = {}

    override val contextMenuDefaultWidth: FloatVar = FloatVar { this@ComboBox.bounds.width.use() }
    override val contextMenuMarkup: Var<Markup?> = Var.bind { this@ComboBox.markup.use() }
    override val contextMenuFont: Var<PaintboxFont> = Var.bind { this@ComboBox.font.use() }
    override val contextMenuItemStrConverter: Var<StringConverter<T>> =
        Var.bind { this@ComboBox.itemStringConverter.use() }

    init {
        this.border.set(Insets(1f))
        this.borderStyle.set(SolidBorder().also { border ->
            border.color.bind { contrastColor.use() }
        })
        this.padding.set(DEFAULT_PADDING)

        @Suppress("LeakingThis")
        HasItemDropdown.setDefaultActionToDeployDropdown(this)
    }

    override fun getDefaultSkinID(): String = COMBOBOX_SKIN_ID

}

open class ComboBoxSkin(element: ComboBox<Any?>) : Skin<ComboBox<Any?>>(element) {

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val paddingBounds = element.paddingZone
        val rectX = paddingBounds.x.get() + originX
        val rectY = originY - paddingBounds.y.get()
        val rectW = paddingBounds.width.get()
        val rectH = paddingBounds.height.get()
        val contentBounds = element.contentZone
        val contentX = contentBounds.x.get() + originX
        val contentY = originY - contentBounds.y.get()
        val contentW = contentBounds.width.get()
        val contentH = contentBounds.height.get()
        val lastPackedColor = batch.packedColor
        val opacity = element.apparentOpacity.get()
        val disabled = element.apparentDisabledState.get()

        val rectColor: Color = ColorStack.getAndPush()
        rectColor.set(element.backgroundColor.getOrCompute())
        rectColor.a *= opacity
        batch.color = rectColor
        val paintboxSpritesheet = PaintboxGame.paintboxSpritesheet
        batch.fillRect(rectX, rectY - rectH, rectW, rectH)

        rectColor.set(element.arrowColor.getOrCompute())
        rectColor.a *= opacity
        if (disabled) {
            rectColor.a *= 0.35f
        }
        batch.color = rectColor
        val arrowSize = min(contentW * element.scaleX.get(), contentH * element.scaleY.get()) * 0.75f
        if (arrowSize > 0f) {
            batch.draw(
                paintboxSpritesheet.downChevronArrow, contentX + contentW - arrowSize,
                contentY - contentH + (contentH - arrowSize) / 2, arrowSize, arrowSize
            )
        }

        batch.packedColor = lastPackedColor
        ColorStack.pop()

        val text = element.internalTextBlock.getOrCompute()
        if (text.runs.isNotEmpty()) {
            val textX = contentX
            val textY = contentY
            val textW = contentW - arrowSize
            val textH = contentH

            val tmpColor = ColorStack.getAndPush()
            tmpColor.set(batch.color).mul(element.textColor.getOrCompute())
            tmpColor.a *= opacity
            if (disabled) {
                rectColor.a *= 0.35f
            }

            text.computeLayoutsIfNeeded()

            val compressX = element.doXCompression.get()
            val align = element.renderAlign.get()
            val xOffset: Float = when {
                Align.isLeft(align) -> 0f
                Align.isRight(align) -> (textW - (if (compressX) min(text.width, textW) else text.width))
                else -> (textW - (if (compressX) min(text.width, textW) else text.width)) / 2f
            }
            val yOffset: Float = when {
                Align.isTop(align) -> textH - text.firstCapHeight
                Align.isBottom(align) -> 0f + (text.height - text.firstCapHeight)
                else -> textH / 2 + text.height / 2 - text.firstCapHeight
            }

            batch.color = tmpColor // Sets the text colour and opacity
            text.drawCompressed(
                batch, textX + xOffset, textY - textH + yOffset,
                if (compressX) (textW) else 0f, element.textAlign.getOrCompute()
            )
            ColorStack.pop()
        }

        batch.packedColor = lastPackedColor
    }

    override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        // NO-OP
    }
}
