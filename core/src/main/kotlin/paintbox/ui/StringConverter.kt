package paintbox.ui


fun interface StringConverter<T> {
    companion object {
        val DEFAULT_STRING_CONVERTER: StringConverter<Any?> = StringConverter { it.toString() }
    }
    
    
    fun convert(item: T): String
    
    operator fun invoke(item: T): String = convert(item) // Code-compatibility
    
}
