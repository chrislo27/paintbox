package paintbox.event

fun interface EventListener<Evt> {
    
    companion object {

        fun <Evt> oneTime(handler: EventListener<Evt>): EventListener<Evt> {
            return object : EventListener<Evt> {
                override fun handle(event: Evt, eventBus: EventBus<Evt>): Boolean {
                    val result = handler.handle(event, eventBus)
                    eventBus.removeListener(this)
                    return result
                }
            }
        }
    }

    /**
     * Returns true if the [event] was consumed. If it was consumed, stops propagation of the event.
     */
    fun handle(event: Evt, eventBus: EventBus<Evt>): Boolean
}
