package paintbox.ui2

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar


interface ReadOnlyLayoutHints {

    val minWidth: ReadOnlyFloatVar
    val prefWidth: ReadOnlyFloatVar
    val maxWidth: ReadOnlyFloatVar

    val minHeight: ReadOnlyFloatVar
    val prefHeight: ReadOnlyFloatVar
    val maxHeight: ReadOnlyFloatVar

}

interface LayoutHints : ReadOnlyLayoutHints {

    companion object {

        /**
         * Can be used for min/max width/height.
         */
        const val USE_PREF_SIZE: Float = Float.NEGATIVE_INFINITY

        /**
         * Can be used for min/pref/max width/height.
         */
        const val USE_COMPUTED_SIZE: Float = -1f
    }

    override val minWidth: FloatVar
    override val prefWidth: FloatVar
    override val maxWidth: FloatVar

    override val minHeight: FloatVar
    override val prefHeight: FloatVar
    override val maxHeight: FloatVar

}

class LayoutHintsImpl : LayoutHints {
    
    override val minWidth: FloatVar = FloatVar(LayoutHints.USE_COMPUTED_SIZE)
    override val prefWidth: FloatVar = FloatVar(LayoutHints.USE_COMPUTED_SIZE)
    override val maxWidth: FloatVar = FloatVar(LayoutHints.USE_COMPUTED_SIZE)

    override val minHeight: FloatVar = FloatVar(LayoutHints.USE_COMPUTED_SIZE)
    override val prefHeight: FloatVar = FloatVar(LayoutHints.USE_COMPUTED_SIZE)
    override val maxHeight: FloatVar = FloatVar(LayoutHints.USE_COMPUTED_SIZE)
}