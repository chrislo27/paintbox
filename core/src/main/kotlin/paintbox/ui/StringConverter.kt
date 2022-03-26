package paintbox.ui

import paintbox.binding.ReadOnlyVar
import paintbox.ui.StringConverter.Companion


/**
 * A functional interface that converts an object [T] into a string.
 * 
 * The default implementation ([Companion.DEFAULT_STRING_CONVERTER]) simply calls [toString].
 */
fun interface StringConverter<T> {
    
    companion object {
        val DEFAULT_STRING_CONVERTER: StringConverter<Any?> = StringConverter { it.toString() }
    }
    
    fun convert(item: T): String
    
}

/**
 * A functional interface that converts an object [T] into a string [ReadOnlyVar].
 */
fun interface StringVarConverter<T> {
    
    companion object {
        val DEFAULT_CONVERTER: StringVarConverter<Any?> = StringConverter.DEFAULT_STRING_CONVERTER.toVarConverter()
    }

    fun toVar(item: T): ReadOnlyVar<String>
    
}

fun <T> StringConverter<T>.toVarConverter(): StringVarConverter<T> = StringVarConverter { item: T ->
    ReadOnlyVar.const(this.convert(item))
} 
