package paintbox.ui2.defaultimpl

import paintbox.binding.Var
import paintbox.ui2.Parent
import paintbox.ui2.UIElement


open class DefaultParent : Parent {

    override val children: Var<List<UIElement>> = Var(emptyList())
    
}
