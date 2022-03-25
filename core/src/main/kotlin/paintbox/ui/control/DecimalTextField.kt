package paintbox.ui.control

import paintbox.PaintboxGame
import paintbox.binding.*
import paintbox.font.PaintboxFont
import paintbox.util.DecimalFormats
import java.text.DecimalFormat


open class DecimalTextField(
    startingValue: Float,
    decimalFormat: DecimalFormat = DecimalFormats["0.0##"],
    font: PaintboxFont = PaintboxGame.gameInstance.debugFont
) : TextField(font) {

    val decimalFormat: Var<DecimalFormat> = Var(decimalFormat)
    val value: ReadOnlyFloatVar = FloatVar(startingValue)
    val minimumValue: FloatVar = FloatVar(Int.MIN_VALUE.toFloat())
    val maximumValue: FloatVar = FloatVar(Int.MAX_VALUE.toFloat())
    val integersOnly: BooleanVar = BooleanVar(false)
    val allowNegatives: ReadOnlyBooleanVar = BooleanVar {
        minimumValue.use() < 0f
    }
    
    private var updating: Boolean = false

    init {
        this.text.set(decimalToStr())
        this.inputFilter.bind {
            val df = this@DecimalTextField.decimalFormat.use()
            val negatives = allowNegatives.use()
            val symbols = df.decimalFormatSymbols
            val intsOnly = integersOnly.use()
            ;
            { c: Char ->
                (negatives && c == symbols.minusSign) || (c in '0'..'9') || (!intsOnly && c == symbols.decimalSeparator)
            }
        }
        hasFocus.addListener { f ->
            if (!f.getOrCompute()) { // When focus is lost, set value
                updating = true
                try { // Set value from text
                    val newValue = this.decimalFormat.getOrCompute().parse(this.text.getOrCompute())?.toFloat()
                    if (newValue != null) {
                        setValue(newValue)
                    }
                } catch (ignored: Exception) {
                }
                this.text.set(decimalToStr())
                updating = false
            }
        }
        value.addListener {
            if (!hasFocus.get() && !updating) {
                updating = true
                this.text.set(decimalToStr())
                updating = false
            }
        }
        integersOnly.addListener {
            if (it.getOrCompute()) {
                val integralValue = value.get().toInt().toFloat()
                if (value.get() != integralValue || this.decimalFormat.getOrCompute().decimalFormatSymbols.decimalSeparator in this.text.getOrCompute()) {
                    this.requestUnfocus()
                    setValue(integralValue)
                    this.text.set(decimalToStr())
                }
            }
        }
        this.setOnRightClick {
            requestFocus()
            text.set("")
        }
    }
    
    fun setValue(value: Float) {
        var coerced = value.coerceIn(minimumValue.get(), maximumValue.get())
        if (integersOnly.get()) {
            coerced = coerced.toInt().toFloat()
        }
        (this.value as FloatVar).set(coerced)
    }

    private fun decimalToStr(): String {
        val currentValue = value.get()
        if (integersOnly.get()) return currentValue.toInt().toString()
        return decimalFormat.getOrCompute().format(currentValue)
    }
}