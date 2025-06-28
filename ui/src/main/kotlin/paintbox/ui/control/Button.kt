package paintbox.ui.control

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import paintbox.PaintboxGame
import paintbox.binding.*
import paintbox.font.*
import paintbox.ui.Corner
import paintbox.ui.UIElement
import paintbox.ui.area.Insets
import paintbox.ui.skin.DefaultSkins
import paintbox.ui.skin.Skin
import paintbox.ui.skin.SkinFactory
import paintbox.util.ColorStack
import paintbox.util.gdxutils.fillRect
import java.util.*
import kotlin.math.min


open class Button(text: String, font: PaintboxFont = UIElement.defaultFont) : Control<Button>(), HasLabelComponent {

    companion object {

        const val BUTTON_SKIN_ID: String = "Button"
        private val DEFAULT_PADDING: Insets = Insets(2f)

        init {
            DefaultSkins.register(BUTTON_SKIN_ID, SkinFactory { element: Button ->
                ButtonSkin(element)
            })
        }

        fun createInternalTextBlockVar(button: Button): Var<TextBlock> {
            return Var {
                val markup: Markup? = button.markup.use()
                (markup?.parse(button.text.use())
                    ?: TextRun(
                        button.font.use(), button.text.use(), Color.WHITE, 1f, 1f,
                        lineHeightScale = button.lineSpacingMultiplier.use()
                    ).toTextBlock()).also { textBlock ->
                    if (button.doLineWrapping.use()) {
                        textBlock.lineWrapping.set(button.contentZone.width.use() / button.scaleX.use())
                    }
                }
            }
        }
    }

    override val text: Var<String> = Var(text)
    override val font: Var<PaintboxFont> = Var(font)
    override val scaleX: FloatVar = FloatVar(1f)
    override val scaleY: FloatVar = FloatVar(1f)
    override val lineSpacingMultiplier: FloatVar = FloatVar(1f)

    val renderAlign: IntVar = IntVar(Align.center)
    val textAlign: Var<TextAlign> = Var { TextAlign.fromInt(renderAlign.use()) }
    val doXCompression: BooleanVar = BooleanVar(true)
    val doLineWrapping: BooleanVar = BooleanVar(false)

    /**
     * The [Markup] object to use. If null, no markup parsing is done. If not null,
     * then the markup determines the TextBlock (and other values like [textColor] are ignored).
     */
    override val markup: Var<Markup?> = Var(null)

    /**
     * Defaults to an auto-generated [TextBlock] with the given [text].
     */
    val internalTextBlock: Var<TextBlock> by lazy { createInternalTextBlockVar(this) }

    constructor(binding: ContextBinding<String>, font: PaintboxFont = UIElement.defaultFont)
            : this("", font) {
        text.bind(binding)
    }

    constructor(bindable: ReadOnlyVar<String>, font: PaintboxFont = UIElement.defaultFont)
            : this("", font) {
        text.bind(bindable)
    }

    init {
        this.padding.set(DEFAULT_PADDING)
    }

    @Suppress("RemoveRedundantQualifierName")
    override fun getDefaultSkinID(): String = Button.BUTTON_SKIN_ID

}

open class ButtonSkin(element: Button) : Skin<Button>(element) {

    val roundedRadius: IntVar = IntVar(2)

    val defaultTextColor: Var<Color> = Var(Color(0f, 0f, 0f, 1f))
    val defaultBgColor: Var<Color> = Var(Color(1f, 1f, 1f, 1f))
    val hoveredTextColor: Var<Color> = Var(Color(0f, 0f, 0f, 1f))
    val hoveredBgColor: Var<Color> = Var(Color(0.95f, 0.95f, 0.95f, 1f))
    val pressedTextColor: Var<Color> = Var(Color(0f, 0f, 0f, 1f))
    val pressedBgColor: Var<Color> = Var(Color(0.75f, 0.95f, 0.95f, 1f))
    val pressedAndHoveredTextColor: Var<Color> = Var(Color(0f, 0f, 0f, 1f))
    val pressedAndHoveredBgColor: Var<Color> = Var(Color(0.75f, 1f, 1f, 1f))
    val disabledTextColor: Var<Color> = Var(Color(0.5f, 0.5f, 0.5f, 1f))
    val disabledBgColor: Var<Color> = Var(Color(0.8f, 0.8f, 0.8f, 1f))

    val roundedCorners: EnumSet<Corner> = EnumSet.allOf(Corner::class.java)

    val textColorToUse: ReadOnlyVar<Color> = Var {
        val pressedState = element.pressedState.use()
        if (element.apparentDisabledState.use()) {
            disabledTextColor.use()
        } else {
            when (pressedState) {
                PressedState.NONE -> defaultTextColor.use()
                PressedState.HOVERED -> hoveredTextColor.use()
                PressedState.PRESSED -> pressedTextColor.use()
                PressedState.PRESSED_AND_HOVERED -> pressedAndHoveredTextColor.use()
            }
        }
    }
    val bgColorToUse: ReadOnlyVar<Color> = Var {
        val pressedState = element.pressedState.use()
        if (element.apparentDisabledState.use()) {
            disabledBgColor.use()
        } else {
            when (pressedState) {
                PressedState.NONE -> defaultBgColor.use()
                PressedState.HOVERED -> hoveredBgColor.use()
                PressedState.PRESSED -> pressedBgColor.use()
                PressedState.PRESSED_AND_HOVERED -> pressedAndHoveredBgColor.use()
            }
        }
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val paddingBounds = element.paddingZone
        val rectX = paddingBounds.x.get() + originX
        val rectY = originY - paddingBounds.y.get()
        val rectW = paddingBounds.width.get()
        val rectH = paddingBounds.height.get()
        val lastPackedColor = batch.packedColor
        val opacity = element.apparentOpacity.get()

        val rectColor: Color = ColorStack.getAndPush()
        rectColor.set(bgColorToUse.getOrCompute())
        rectColor.a *= opacity
        batch.color = rectColor
        var roundedRad = roundedRadius.get()
        val paintboxSpritesheet = PaintboxGame.gameInstance.staticAssets.paintboxSpritesheet
        val spritesheetFill: TextureRegion = paintboxSpritesheet.fill
        if (roundedRad > rectW / 2f) {
            roundedRad = (rectW / 2f).toInt()
        }
        if (roundedRad > rectH / 2f) {
            roundedRad = (rectH / 2f).toInt()
        }
        if (roundedRad <= 0) {
            batch.fillRect(rectX, rectY - rectH, rectW, rectH)
        } else {
            val roundedRect: TextureRegion = paintboxSpritesheet.getRoundedCornerForRadius(roundedRad)
            batch.fillRect(
                rectX + roundedRad,
                rectY - rectH + roundedRad,
                rectW - roundedRad * 2,
                rectH - roundedRad * 2
            )
            batch.fillRect(rectX, rectY - rectH + roundedRad, (roundedRad).toFloat(), rectH - roundedRad * 2)
            batch.fillRect(
                rectX + rectW - roundedRad,
                rectY - rectH + roundedRad,
                (roundedRad).toFloat(),
                rectH - roundedRad * 2
            )
            batch.fillRect(rectX + roundedRad, rectY - rectH, rectW - roundedRad * 2, (roundedRad).toFloat())
            batch.fillRect(rectX + roundedRad, rectY - roundedRad, rectW - roundedRad * 2, (roundedRad).toFloat())
            val roundedCornersSet = roundedCorners
            batch.draw(
                if (Corner.TOP_LEFT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX, rectY - roundedRad, (roundedRad).toFloat(), (roundedRad).toFloat()
            ) // TL
            batch.draw(
                if (Corner.BOTTOM_LEFT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX, rectY - rectH + roundedRad, (roundedRad).toFloat(), (-roundedRad).toFloat()
            ) // BL
            batch.draw(
                if (Corner.TOP_RIGHT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX + rectW, rectY - roundedRad, (-roundedRad).toFloat(), (roundedRad).toFloat()
            ) // TR
            batch.draw(
                if (Corner.BOTTOM_RIGHT in roundedCornersSet) roundedRect else spritesheetFill,
                rectX + rectW, rectY - rectH + roundedRad, (-roundedRad).toFloat(), (-roundedRad).toFloat()
            ) // BR
        }
        batch.packedColor = lastPackedColor
        ColorStack.pop()

        val text = element.internalTextBlock.getOrCompute()
        if (text.runs.isNotEmpty()) {
            val textBounds = element.contentZone
            val textX = textBounds.x.get() + originX
            val textY = originY - textBounds.y.get()
            val textW = textBounds.width.get()
            val textH = textBounds.height.get()

            val tmpColor = ColorStack.getAndPush()
            tmpColor.set(batch.color).mul(textColorToUse.getOrCompute())
            tmpColor.a *= opacity

            text.computeLayoutsIfNeeded()

            val compressX = element.doXCompression.get()
            val align = element.renderAlign.get()
            val scaleX = element.scaleX.get()
            val scaleY = element.scaleY.get()
            val textWidth = text.width * scaleX
            val textHeight = text.height * scaleY
            val xOffset: Float = when {
                Align.isLeft(align) -> 0f
                Align.isRight(align) -> (textW - ((if (compressX) (min(textWidth, textW)) else textWidth)))
                else -> (textW - (if (compressX) min(textWidth, textW) else textWidth)) / 2f
            }
            val firstCapHeight = text.firstCapHeight * scaleY
            val yOffset: Float = when {
                Align.isTop(align) -> textH - firstCapHeight
                Align.isBottom(align) -> 0f + (textHeight - firstCapHeight)
                else -> ((textH + textHeight) / 2 - firstCapHeight)
            }

            batch.color = tmpColor // Sets the text colour and opacity
            text.drawCompressed(
                batch, textX + xOffset, textY - textH + yOffset,
                if (compressX) (textW) else 0f, element.textAlign.getOrCompute(), scaleX, scaleY
            )
            ColorStack.pop()
        }

        batch.packedColor = lastPackedColor
    }

    override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        // NO-OP
    }

}