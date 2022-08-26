package paintbox.util


object MemoryUtils {
    
    // https://stackoverflow.com/questions/2015463/how-to-view-the-current-heap-size-that-an-application-is-using

    /**
     * Currently used heap memory in bytes.
     */
    val usedMemoryB: Long get() = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
    val usedMemoryKiB: Long get() = (usedMemoryB / 1024)
    val usedMemoryMiB: Long get() = (usedMemoryB / 1_048_578)

    /**
     * Maximum amount of memory the JVM will attempt to use in bytes.
     */
    val maxMemoryB: Long get() = (Runtime.getRuntime().maxMemory())
    val maxMemoryKiB: Long get() = (maxMemoryB / 1024)
    val maxMemoryMiB: Long get() = (maxMemoryB / 1_048_578)

    /**
     * Amount of free memory in the JVM in byte.s
     */
    val freeMemoryB: Long get() = (Runtime.getRuntime().freeMemory())
    val freeMemoryKiB: Long get() = (freeMemoryB / 1024)
    val freeMemoryMiB: Long get() = (freeMemoryB / 1_048_578)
    
    /**
     * Amount of memory guaranteed to be available for use by the JVM in bytes.
     */
    val committedMemoryB: Long get() = (Runtime.getRuntime().totalMemory())
    val committedMemoryKiB: Long get() = (committedMemoryB / 1024)
    val committedMemoryMiB: Long get() = (committedMemoryB / 1_048_578)
    
}
