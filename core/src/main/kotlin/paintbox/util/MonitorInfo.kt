package paintbox.util

import com.badlogic.gdx.Graphics


data class MonitorInfo(val name: String, val virtualX: Int, val virtualY: Int) {
    
    companion object {
        private val encodedRegex: Regex = """(\d+)\|(\d+)\|(.*)""".toRegex()
        
        fun fromMonitor(monitor: Graphics.Monitor): MonitorInfo {
            return MonitorInfo(monitor.name ?: "", monitor.virtualX, monitor.virtualY)
        }

        fun fromEncodedString(encoded: String): MonitorInfo? {
            val match = encodedRegex.matchEntire(encoded)
            return if (match == null) {
                null
            } else {
                try {
                    MonitorInfo(match.groupValues[3], match.groupValues[1].toInt(), match.groupValues[2].toInt())
                } catch (ignored: NumberFormatException) {
                    null
                }
            }
        }
    }

    fun doesMonitorMatch(monitor: Graphics.Monitor): Boolean {
        return monitor.name == this.name && monitor.virtualX == this.virtualX && monitor.virtualY == this.virtualY
    }
    
    fun toEncodedString(): String = "${virtualX}|${virtualY}|$name"
    
    override fun toString(): String {
        return "$name at ${virtualX}x${virtualY}"
    }
}
