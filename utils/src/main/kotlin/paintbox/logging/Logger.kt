package paintbox.logging

import java.io.PrintStream


open class Logger {

    enum class LogLevel(val levelNumber: Int) {
        ALL(Int.MIN_VALUE),
        TRACE(-1),
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3),
        NONE(Int.MAX_VALUE);
        
        fun getLogName(): String {
            return when (this) {
                TRACE -> "TRC"
                DEBUG -> "DBG"
                INFO -> "INF"
                WARN -> "WRN"
                ERROR -> "ERR"
                else -> this.name
            }
        }
    }

    val msTimeStarted: Long = System.currentTimeMillis()
    val msTimeElapsed: Long
        get() = System.currentTimeMillis() - msTimeStarted

    var loggingLevel: LogLevel = LogLevel.DEBUG

    protected open fun getTimestamp(): String {
        val millis = msTimeElapsed
        val second = (millis / 1000) % 60
        val minute = millis / (1000 * 60) % 60
        val hour = millis / (1000 * 60 * 60) % 24
        
        return String.format(
            "%02d:%02d:%02d.%03d",
            hour,
            minute,
            second,
            millis % 1000
        )
    }
    
    protected open fun defaultPrint(level: LogLevel, msg: String, tag: String, throwable: Throwable?) {
        val text = "${getTimestamp()} [${level.getLogName()}][${Thread.currentThread().name}] ${if (tag.isEmpty()) "" else "[$tag] "}$msg"

        val printStream: PrintStream = if (level.ordinal >= LogLevel.WARN.ordinal) System.err else System.out
        
        printStream.println(text)

        throwable?.printStackTrace(printStream)
    }

    fun log(logLevel: LogLevel, msg: String, tag: String = "", throwable: Throwable? = null) {
        if (loggingLevel.levelNumber <= logLevel.levelNumber) {
            defaultPrint(logLevel, msg, tag, throwable)
        }
    }

    fun trace(msg: String, tag: String = "", throwable: Throwable? = null) {
        log(LogLevel.TRACE, msg, tag, throwable)
    }

    fun debug(msg: String, tag: String = "", throwable: Throwable? = null) {
        log(LogLevel.DEBUG, msg, tag, throwable)
    }

    fun info(msg: String, tag: String = "", throwable: Throwable? = null) {
        log(LogLevel.INFO, msg, tag, throwable)
    }

    fun warn(msg: String, tag: String = "", throwable: Throwable? = null) {
        log(LogLevel.WARN, msg, tag, throwable)
    }

    fun error(msg: String, tag: String = "", throwable: Throwable? = null) {
        log(LogLevel.ERROR, msg, tag, throwable)
    }
}