package paintbox.ui2.defaultimpl

import paintbox.binding.FloatVar
import paintbox.binding.Var
import paintbox.ui2.*


open class DefaultUIBounds : UIBounds {

    final override val bounds: Bounds = BoundsImpl()
    final override val margin: Var<Insets> = Var(Insets.ZERO)
    final override val border: Var<Insets> = Var(Insets.ZERO)
    final override val padding: Var<Insets> = Var(Insets.ZERO)
    final override val contentOffsetX: FloatVar = FloatVar(0f)
    final override val contentOffsetY: FloatVar = FloatVar(0f)
    final override val marginZone: ReadOnlyBounds get() = super.marginZone
    final override val borderZone: ReadOnlyBounds = UIBounds.createBorderZone(marginZone, margin)
    final override val paddingZone: ReadOnlyBounds = UIBounds.createPaddingZone(marginZone, margin, border)
    final override val contentZone: ReadOnlyBounds = UIBounds.createContentZone(marginZone, margin, border, padding, contentOffsetX, contentOffsetY)
}