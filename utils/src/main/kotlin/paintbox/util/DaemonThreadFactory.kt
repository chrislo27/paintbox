package paintbox.util

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory


/**
 * A [ThreadFactory] that uses [Executors.defaultThreadFactory] and makes its created threads daemon threads.
 */
class DaemonThreadFactory : ThreadFactory {

    override fun newThread(r: Runnable): Thread {
        return Executors.defaultThreadFactory().newThread(r).apply {
            this.isDaemon = true
        }
    }
}