package paintbox.ui2.defaultimpl

import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.ui2.Parent
import paintbox.ui2.SceneRoot
import paintbox.ui2.UIBounds
import paintbox.ui2.UIElement


open class DefaultUIElement(
    uiBounds: UIBounds = DefaultUIBounds()
) : UIElement, UIBounds by uiBounds {

    private val _parent: Var<Parent?> = Var(null)
    override val parent: ReadOnlyVar<Parent?> get() = _parent
    
    override val sceneRoot: ReadOnlyVar<SceneRoot?> = Var {
        (parent.use() as? UIElement)?.sceneRoot?.use()
    }
    
    override fun setParent(newParent: Parent?) {
        val oldParent = _parent.getOrCompute()
        if (oldParent != null) {
            onRemovedFromParent(oldParent)
        }

        _parent.set(newParent)

        if (newParent != null) {
            onAddedToParent(newParent)
        }
    }
    
    override fun toString(): String {
        return this.defaultToString()
    }
}