package paintbox.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import java.util.LinkedList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


abstract class ResourceStack<T>(initialCapacity: Int = 64) {

    private val pool: InternalPool = InternalPool(initialCapacity)
    private val stack: LinkedList<T & Any> = LinkedList()
    
    val numInStack: Int get() = stack.size
    var peakCount: Int = 0
        private set
    
    
    /**
     * Gets a new object from the pool and returns it. This puts it on the resource stack.
     */
    fun getAndPush(): T & Any {
        val obtained = pool.obtain() ?: error("[Thread ${Thread.currentThread().name}] pool.obtain returned null. This could be a threading issue. numFree: ${pool.free} ResourceStack: ${this.javaClass.name}")
        stack.add(obtained)
        if (peakCount < stack.size) {
            peakCount = stack.size
        }
        return obtained
    }

    /**
     * Pops the last resource off the stack.
     * @return True if something was popped off the stack, false otherwise
     */
    fun pop(): T? {
        if (stack.isEmpty()) return null
        val last = stack.removeLast() ?: error("[Thread ${Thread.currentThread().name}] Stack's last item from stack.removeLast() was null. This could be a threading issue. numFree: ${pool.free} ResourceStack: ${this.javaClass.name}")
        pool.free(last)
        return last
    }

    protected abstract fun newObject(): T & Any
    
    protected abstract fun resetWhenFreed(obj: T & Any)

    /**
     * Uses a pooled resource inside the [action] block. This automatically frees the obtained resource.
     */
    @OptIn(ExperimentalContracts::class)
    inline fun use(action: (obj: T) -> Unit) {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        val obj = getAndPush()
        action(obj)
        pop()
    }
    
    fun resetPeakCount(): Int {
        val old = peakCount
        peakCount = 0
        return old
    }

    private inner class InternalPool(initialCapacity: Int) : Pool<T>(initialCapacity) {
        override fun newObject(): T & Any {
            return this@ResourceStack.newObject()
        }

        override fun reset(`object`: T?) {
            if (`object` != null) {
                this@ResourceStack.resetWhenFreed(`object`)
            }
        }
    }
}

/**
 * A convenience singleton for implementing temporary, pooled [com.badlogic.gdx.graphics.Color]s in a stack method.
 */
object ColorStack : ResourceStack<Color>() {
    override fun newObject(): Color {
        return Color(1f, 1f, 1f, 1f)
    }

    override fun resetWhenFreed(obj: Color) {
        obj.set(1f, 1f, 1f, 1f)
    }
}

object RectangleStack : ResourceStack<Rectangle>() {
    override fun newObject(): Rectangle {
        return Rectangle()
    }

    override fun resetWhenFreed(obj: Rectangle) {
        obj.set(0f, 0f, 0f, 0f)
    }
}

object Vector2Stack : ResourceStack<Vector2>() {
    override fun newObject(): Vector2 {
        return Vector2()
    }

    override fun resetWhenFreed(obj: Vector2) {
        obj.set(0f, 0f)
    }
}

object Vector3Stack : ResourceStack<Vector3>() {
    override fun newObject(): Vector3 {
        return Vector3()
    }

    override fun resetWhenFreed(obj: Vector3) {
        obj.set(0f, 0f, 0f)
    }
}

object Matrix4Stack : ResourceStack<Matrix4>() {
    override fun newObject(): Matrix4 {
        return Matrix4()
    }

    override fun resetWhenFreed(obj: Matrix4) {
    }
}
