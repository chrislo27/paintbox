package paintbox.ui2.defaultimpl

import paintbox.binding.FloatVar
import paintbox.ui2.Bounds

class BoundsImpl(
    override val x: FloatVar = FloatVar(0f),
    override val y: FloatVar = FloatVar(0f),
    override val width: FloatVar = FloatVar(0f),
    override val height: FloatVar = FloatVar(0f),
) : Bounds