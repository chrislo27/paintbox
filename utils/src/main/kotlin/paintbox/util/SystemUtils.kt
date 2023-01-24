package paintbox.util

import java.util.*


object SystemUtils {

    enum class OSType {
        WINDOWS,
        LINUX,
        MACOS,
        OTHER;
    }

    val OS_NAME: String = System.getProperty("os.name")
    private val OS_NAME_LOWER: String = OS_NAME.lowercase(Locale.ROOT)

    val OS: OSType = when {
        OS_NAME_LOWER.startsWith("windows") -> OSType.WINDOWS
        OS_NAME_LOWER.startsWith("linux") -> OSType.LINUX
        OS_NAME_LOWER.indexOf("mac") != -1 -> OSType.MACOS
        else -> OSType.OTHER
    }

    fun isWindows(): Boolean = OS == OSType.WINDOWS
    fun isLinux(): Boolean = OS == OSType.LINUX
    fun isMacOS(): Boolean = OS == OSType.MACOS

}