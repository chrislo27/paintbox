package paintbox.ui2

import paintbox.binding.Var


@Suppress("RedundantModalityModifier")
final class SceneRoot : UIElement() {
    // TODO implement
    
    init {
        (this.sceneRoot as Var).set(this)
        setParent(null)
    }

    override fun setParent(parent: UIElement?) {
        super.setParent(null)
    }
}