package paintbox.ui.animation

import paintbox.binding.ContextBinding
import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar
import paintbox.binding.VarChangedListener


class TransitioningFloatVar(
    private val animationHandler: AnimationHandler,
    binding: ContextBinding<Float>,
    animationFactory: (currentValue: Float, targetValue: Float) -> Animation?,
) : ReadOnlyFloatVar {
    
    private val immediateValue: FloatVar = FloatVar(binding)
    
    private val transitioningVar: FloatVar
    
    init {
        immediateValue.addListener { v ->
            val targetValue = v.getOrCompute()
            val newAnimation = animationFactory(transitioningVar.get(), targetValue)
            if (newAnimation != null) {
                animationHandler.enqueueAnimation(newAnimation, transitioningVar)
            } else {
                transitioningVar.set(targetValue)
            }
        }
        
        transitioningVar = FloatVar(immediateValue) // Initially bound to immediateValue for first initialization
    }

    override fun get(): Float = transitioningVar.get()

    override fun addListener(listener: VarChangedListener<Float>) = transitioningVar.addListener(listener)

    override fun removeListener(listener: VarChangedListener<Float>) = transitioningVar.removeListener(listener)

    override fun invalidate() = transitioningVar.invalidate()
}