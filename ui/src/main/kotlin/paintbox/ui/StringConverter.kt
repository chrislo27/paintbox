package paintbox.ui

import paintbox.binding.ReadOnlyVar


/**
 * A functional interface that converts an object [T] into a string.
 *
 * The default implementation ([Companion.createDefaultConverter]) simply calls [toString].
 */
fun interface StringConverter<T> {

    companion object {

        private val DEFAULT_CONVERTER: StringConverter<Any?> = StringConverter { it.toString() }
        
        @Suppress("UNCHECKED_CAST")
        fun <T> createDefaultConverter(): StringConverter<T> = DEFAULT_CONVERTER as StringConverter<T>
    }

    fun convert(item: T): String

}

/**
 * A functional interface that converts an object [T] into a string [ReadOnlyVar].
 */
fun interface StringVarConverter<T> {

    companion object {

        private val DEFAULT_CONVERTER: StringVarConverter<Any?> =
            StringVarConverter { ReadOnlyVar.const(it.toString()) }

        @Suppress("UNCHECKED_CAST")
        fun <T> createDefaultConverter(): StringVarConverter<T> =
            DEFAULT_CONVERTER as StringVarConverter<T>
    }

    fun toVar(item: T): ReadOnlyVar<String>

}

fun <T> StringConverter<T>.toVarConverter(): StringVarConverter<T> = StringVarConverter { item: T ->
    ReadOnlyVar.const(this.convert(item))
} 
