package paintbox.event

import paintbox.event.defaultimpl.DefaultEventBus


interface EventBus<Evt> {
    
    companion object {
        
        operator fun <Evt> invoke(): EventBus<Evt> = DefaultEventBus()
    }

    fun addListener(listener: EventListener<Evt>)
    
    fun removeListener(listener: EventListener<Evt>)

    /**
     * Fires the [event], with listeners being notified in the REVERSE order they were added.
     */
    fun fire(event: Evt)
}
