package paintbox.prefs

import com.badlogic.gdx.Preferences
import paintbox.binding.BooleanVar
import paintbox.binding.IntVar
import paintbox.binding.Var
import paintbox.util.WindowSize
import java.time.LocalDate
import java.time.format.DateTimeFormatter

abstract class KeyValue<T>(val key: String, val defaultValue: T) {
    
    companion object {
        fun Preferences.getInt(kv: KeyValue<kotlin.Int>) {
            val prefs: Preferences = this
            if (prefs.contains(kv.key)) {
                kv.value.set(prefs.getInteger(kv.key, kv.defaultValue))
            }
        }

        fun Preferences.getIntCoerceIn(kv: KeyValue<kotlin.Int>, min: kotlin.Int, max: kotlin.Int) {
            val prefs: Preferences = this
            if (prefs.contains(kv.key)) {
                kv.value.set(prefs.getInteger(kv.key, kv.defaultValue).coerceIn(min, max))
            }
        }

        fun Preferences.putInt(kv: KeyValue<kotlin.Int>): Preferences {
            val prefs: Preferences = this
            return prefs.putInteger(kv.key, kv.value.getOrCompute())
        }

        fun Preferences.getBoolean(kv: KeyValue<Boolean>) {
            val prefs: Preferences = this
            if (prefs.contains(kv.key)) {
                kv.value.set(prefs.getBoolean(kv.key, kv.defaultValue))
            }
        }

        fun Preferences.putBoolean(kv: KeyValue<Boolean>): Preferences {
            val prefs: Preferences = this
            return prefs.putBoolean(kv.key, kv.value.getOrCompute())
        }

        fun Preferences.getString(kv: KeyValue<String>, defaultValue: String = kv.defaultValue) {
            val prefs: Preferences = this
            if (prefs.contains(kv.key)) {
                kv.value.set(prefs.getString(kv.key, defaultValue))
            }
        }

        fun Preferences.putString(kv: KeyValue<String>): Preferences {
            val prefs: Preferences = this
            return prefs.putString(kv.key, kv.value.getOrCompute())
        }

        fun Preferences.getWindowSize(kv: KeyValue<paintbox.util.WindowSize>) {
            val prefs: Preferences = this
            if (prefs.contains(kv.key)) {
                val str = prefs.getString(kv.key)
                try {
                    val width = str.substringBefore('x').toInt()
                    val height = str.substringAfter('x').toInt()
                    kv.value.set(WindowSize(width, height))
                } catch (ignored: Exception) {
                    kv.value.set(kv.defaultValue)
                }
            }
        }

        fun Preferences.putWindowSize(kv: KeyValue<paintbox.util.WindowSize>): Preferences {
            val prefs: Preferences = this
            val windowSize = kv.value.getOrCompute()
            return prefs.putString(kv.key, "${windowSize.width}x${windowSize.height}")
        }

        fun Preferences.getLocalDate(kv: KeyValue<LocalDate>) {
            val prefs: Preferences = this
            if (prefs.contains(kv.key)) {
                val str = prefs.getString(kv.key)
                try {
                    val localDate: LocalDate = LocalDate.parse(str, DateTimeFormatter.ISO_DATE)
                    kv.value.set(localDate)
                } catch (ignored: Exception) {
                    kv.value.set(LocalDate.MIN)
                }
            }
        }

        fun Preferences.putLocalDate(kv: KeyValue<LocalDate>): Preferences {
            val prefs: Preferences = this
            return prefs.putString(kv.key, kv.value.getOrCompute().format(DateTimeFormatter.ISO_DATE))
        }

        fun Preferences.getMonitorInfo(kv: KeyValue<paintbox.util.MonitorInfo?>) {
            val prefs: Preferences = this
            if (prefs.contains(kv.key)) {
                val str = prefs.getString(kv.key)
                kv.value.set(paintbox.util.MonitorInfo.fromEncodedString(str))
            }
        }

        fun Preferences.putMonitorInfo(kv: KeyValue<paintbox.util.MonitorInfo?>): Preferences {
            val prefs: Preferences = this
            val mi = kv.value.getOrCompute()
            return prefs.putString(kv.key, mi?.toEncodedString() ?: "")
        }
    }

    abstract val value: Var<T>

    abstract fun load(prefs: Preferences)
    abstract fun persist(prefs: Preferences)

    class Int(key: String, defaultValue: kotlin.Int, val min: kotlin.Int, val max: kotlin.Int)
        : KeyValue<kotlin.Int>(key, defaultValue) {

        override val value: IntVar = IntVar(defaultValue)

        override fun load(prefs: Preferences) {
            prefs.getIntCoerceIn(this, min, max)
        }

        override fun persist(prefs: Preferences) {
            prefs.putInt(this)
        }
    }

    class Bool(key: String, defaultValue: kotlin.Boolean)
        : KeyValue<Boolean>(key, defaultValue) {

        override val value: BooleanVar = BooleanVar(defaultValue)

        override fun load(prefs: Preferences) {
            prefs.getBoolean(this)
        }

        override fun persist(prefs: Preferences) {
            prefs.putBoolean(this)
        }
    }

    class Str(key: String, defaultValue: String)
        : KeyValue<String>(key, defaultValue) {

        override val value: Var<String> = Var(defaultValue)

        override fun load(prefs: Preferences) {
            prefs.getString(this)
        }

        override fun persist(prefs: Preferences) {
            prefs.putString(this)
        }
    }

    class WindowSize(key: String, defaultValue: paintbox.util.WindowSize)
        : KeyValue<paintbox.util.WindowSize>(key, defaultValue) {

        override val value: Var<paintbox.util.WindowSize> = Var(defaultValue)

        override fun load(prefs: Preferences) {
            prefs.getWindowSize(this)
        }

        override fun persist(prefs: Preferences) {
            prefs.putWindowSize(this)
        }
    }

    class MonitorInfo(key: String, defaultValue: paintbox.util.MonitorInfo?)
        : KeyValue<paintbox.util.MonitorInfo?>(key, defaultValue) {

        override val value: Var<paintbox.util.MonitorInfo?> = Var(defaultValue)

        override fun load(prefs: Preferences) {
            prefs.getMonitorInfo(this)
        }

        override fun persist(prefs: Preferences) {
            prefs.putMonitorInfo(this)
        }
    }
}