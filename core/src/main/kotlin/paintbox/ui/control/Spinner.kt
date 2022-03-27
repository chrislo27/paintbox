package paintbox.ui.control

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import paintbox.PaintboxGame
import paintbox.binding.BooleanVar
import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar
import paintbox.font.PaintboxFont
import paintbox.ui.Anchor
import paintbox.ui.Pane
import paintbox.ui.UIElement
import paintbox.ui.skin.DefaultSkins
import paintbox.ui.skin.Skin
import paintbox.ui.skin.SkinFactory


/**
 * A spinner is a complex control for a [DecimalTextField].
 */
open class Spinner(initialValue: Float, integerMode: Boolean = false,
                   font: PaintboxFont = UIElement.defaultFont)
    : Control<Spinner>() {
    
    companion object {
        const val SPINNER_SKIN_ID: String = "Spinner"
        
        const val INCREMENT_AMOUNT: Float = 1f
        
        init {
            DefaultSkins.register(SPINNER_SKIN_ID, SkinFactory { element: Spinner ->
                Spinner.SpinnerSkin(element)
            })
        }
    }

    val textField: DecimalTextField = DecimalTextField(initialValue, font = font)
    
    val integerMode: BooleanVar get() = textField.integersOnly
    val incrementAmount: FloatVar = FloatVar(INCREMENT_AMOUNT)
    val minimumValue: FloatVar get() = textField.minimumValue
    val maximumValue: FloatVar get() = textField.maximumValue
    val currentValue: ReadOnlyFloatVar get() = textField.value
    
    val upButton: Button
    val downButton: Button
    val buttonPane: Pane
    val fieldContainer: Pane
    
    
    constructor(initialValue: Int, font: PaintboxFont = UIElement.defaultFont)
            : this(initialValue.toFloat(), true, font)
    
    init {
        textField.integersOnly.set(integerMode)
        buttonPane = Pane().apply { 
            Anchor.TopRight.configure(this)
            this.bindWidthToSelfHeight()
        }
        addChild(buttonPane)
        fieldContainer = Pane().apply { 
            Anchor.TopLeft.configure(this)
            this.bindWidthToParent { -(buttonPane.bounds.width.use()) }
        }
        addChild(fieldContainer)
        
        textField.apply {
            setValue(initialValue)
        }
        fieldContainer += textField
        
        upButton = Button("^").apply { 
            Anchor.TopLeft.configure(this)
            this.disabled.bind { currentValue.use() >= maximumValue.use() }
            this.bindHeightToParent(multiplier = 0.5f)
            this.setOnAction { 
                increment()
            }
        }
        buttonPane.addChild(upButton)
        downButton = Button("v").apply { 
            Anchor.BottomLeft.configure(this)
            this.disabled.bind { currentValue.use() <= minimumValue.use() }
            this.bindHeightToParent(multiplier = 0.5f)
            this.setOnAction {
                decrement()
            }
        }
        buttonPane.addChild(downButton)
        
    }
    
    fun increment() {
        val cv = currentValue.get()
        val mv = maximumValue.get()
        if (cv < mv) {
            setValue((cv + incrementAmount.get()).coerceAtMost(mv))
        }
    }
    
    fun decrement() {
        val cv = currentValue.get()
        val mv = minimumValue.get()
        if (cv > mv) {
            setValue((cv - incrementAmount.get()).coerceAtLeast(mv))
        }
    }
    
    fun setValue(value: Float) {
        textField.setValue(value)
    }
    
    override fun getDefaultSkinID(): String {
        return SPINNER_SKIN_ID
    }
    
    open class SpinnerSkin(element: Spinner) : Skin<Spinner>(element) {
        override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        }

        override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        }
    }
}