package paintbox.ui.control

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.font.Markup
import paintbox.font.PaintboxFont


/**
 * Indicates common text label-related fields for a [Control].
 *
 * Shared by [TextLabel], [Button], [ComboBox], etc.
 */
interface HasLabelComponent {

    val text: ReadOnlyVar<String>
    val font: Var<PaintboxFont>
    val markup: Var<Markup?>
    val scaleX: FloatVar
    val scaleY: FloatVar
    val lineSpacingMultiplier: FloatVar


    fun setScaleXY(scaleXY: Float) {
        this.scaleX.set(scaleXY)
        this.scaleY.set(scaleXY)
    }

    /**
     * Binds [markup] to [Markup.createWithSingleFont] using this [font].
     */
    fun useSingleFontMarkup(lenientMode: Boolean = false) {
        markup.bind { Markup.createWithSingleFont(font.use(), lenientMode) }
    }

}