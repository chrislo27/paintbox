package paintbox.ui

import paintbox.binding.Var


open class PaneWithTooltip : Pane(), HasTooltip {

    override val tooltipElement: Var<UIElement?> = Var(null)
}