package paintbox.ui2

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar


interface ReadOnlyLayoutDimensions {

    val minimumWidth: ReadOnlyFloatVar
    val preferredWidth: ReadOnlyFloatVar
    val maximumWidth: ReadOnlyFloatVar

    val minimumHeight: ReadOnlyFloatVar
    val preferredHeight: ReadOnlyFloatVar
    val maximumHeight: ReadOnlyFloatVar

}

interface LayoutDimensions : ReadOnlyLayoutDimensions {

    companion object {

        /**
         * Can be used for [minimumWidth]/[maximumWidth]/[minimumHeight]/[maximumHeight].
         */
        const val USE_PREF_SIZE: Float = Float.NEGATIVE_INFINITY

        /**
         * Can be used for [minimumWidth]/[preferredWidth]/[maximumWidth]/[minimumHeight]/[preferredHeight]/[maximumHeight].
         */
        const val USE_COMPUTED_SIZE: Float = -1f
    }

    override val minimumWidth: FloatVar
    override val preferredWidth: FloatVar
    override val maximumWidth: FloatVar

    override val minimumHeight: FloatVar
    override val preferredHeight: FloatVar
    override val maximumHeight: FloatVar

}

class LayoutDimensionsImpl : LayoutDimensions {
    
    override val minimumWidth: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val preferredWidth: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val maximumWidth: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)

    override val minimumHeight: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val preferredHeight: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
    override val maximumHeight: FloatVar = FloatVar(LayoutDimensions.USE_COMPUTED_SIZE)
}