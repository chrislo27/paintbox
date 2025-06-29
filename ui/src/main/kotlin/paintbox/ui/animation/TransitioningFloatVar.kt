package paintbox.ui.animation

import paintbox.binding.ContextBinding
import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar
import paintbox.binding.VarChangedListener


class TransitioningFloatVar private constructor(
    private val animationHandler: AnimationHandler,
    immediateValueVar: FloatVar,
    animationFactory: (currentValue: Float, targetValue: Float) -> Animation?,
) : ReadOnlyFloatVar {
    
    constructor(
        animationHandler: AnimationHandler, 
        binding: ContextBinding<Float>, 
        animationFactory: (currentValue: Float, targetValue: Float) -> Animation?
    ) : this(animationHandler, FloatVar(binding), animationFactory)
    
    constructor(
        animationHandler: AnimationHandler, 
        bindable: ReadOnlyFloatVar, 
        animationFactory: (currentValue: Float, targetValue: Float) -> Animation?
    ) : this(animationHandler, FloatVar(bindable), animationFactory)

    private val immediateValue: FloatVar = immediateValueVar
    private val transitioningVar: FloatVar
    
    init {
        this.immediateValue.addListener { v ->
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