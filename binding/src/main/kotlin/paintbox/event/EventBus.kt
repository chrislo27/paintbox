package paintbox.event


class EventBus<Evt> {
    
    private var listeners: List<EventListener<Evt>> = emptyList()
    
    fun addListener(listener: EventListener<Evt>) {
        listeners += listener
    }
    
    fun removeListener(listener: EventListener<Evt>) {
        listeners -= listener
    }
    
    fun fire(evt: Evt) {
        for (listener in listeners.asReversed()) {
            val consumed = listener.handle(evt, this)
            if (consumed) break
        }
    }
}
