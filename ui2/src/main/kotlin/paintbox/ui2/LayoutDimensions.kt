package paintbox.ui2

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar


interface ReadOnlyLayoutDimensions {

    val minWidth: ReadOnlyFloatVar
    val prefWidth: ReadOnlyFloatVar
    val maxWidth: ReadOnlyFloatVar

    val minHeight: ReadOnlyFloatVar
    val prefHeight: ReadOnlyFloatVar
    val maxHeight: ReadOnlyFloatVar

}

interface LayoutDimensions : ReadOnlyLayoutDimensions {

    companion object {

        /**
         * Can be used for [minWidth]/[maxWidth]/[minHeight]/[maxHeight].
         */
        const val USE_PREF_SIZE: Float = Float.NEGATIVE_INFINITY

        /**
         * Can be used for [minWidth]/[prefWidth]/[maxWidth]/[minHeight]/[prefHeight]/[maxHeight].
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

