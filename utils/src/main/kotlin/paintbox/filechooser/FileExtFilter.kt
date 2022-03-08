package paintbox.filechooser


/**
 * A file extension filter. The [extensions] should be strings in a format like `png`, `ogg`, etc.
 */
data class FileExtFilter(val description: String, val extensions: List<String>) {
    fun copyWithExtensionsInDesc(): FileExtFilter =
        this.copy(description = "$description (${
            extensions.joinToString(separator = ", ") {
                ".$it"
            }
        })", extensions = extensions)
}