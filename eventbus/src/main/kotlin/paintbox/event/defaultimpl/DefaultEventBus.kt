package paintbox.event.defaultimpl

import paintbox.event.EventBus
import paintbox.event.EventListener

class DefaultEventBus<Evt> : EventBus<Evt> {
    
    private var listeners: List<EventListener<Evt>> = emptyList()
    
    override fun addListener(listener: EventListener<Evt>) {
        listeners += listener
    }
    
    override fun removeListener(listener: EventListener<Evt>) {
        listeners -= listener
    }
    
    override fun fire(event: Evt) {
        for (listener in listeners.asReversed()) {
            val consumed = listener.handle(event, this)
            if (consumed) break
        }
    }
}