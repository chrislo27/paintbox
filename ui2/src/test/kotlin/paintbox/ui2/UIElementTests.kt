package paintbox.ui2

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import paintbox.binding.Var
import kotlin.test.Test


class UIElementTests {
    
    @Test
    fun `sceneRoot property is backed by a Var`() {
        // Arrange
        val element = UIElement()
        
        // Assert
        assertThat(element.sceneRoot, instanceOf(Var::class.java))
    }
    
    @Test
    fun `adding an element as a child sets its parent property`() {
        // Arrange
        val parent = UIElement()
        val child = UIElement()
        
        // Act
        parent.addChild(child)
        
        // Assert
        assertThat(parent.children.getOrCompute(), contains(child))
        assertThat(child.parent.getOrCompute(), `is`(parent))
    }
    
    @Test
    fun `removing an element as a child nulls its parent property`() {
        // Arrange
        val parent = UIElement()
        val child = UIElement()
        
        parent.addChild(child)
        
        // Act
        parent.removeChild(child)
        
        // Assert
        assertThat(parent.children.getOrCompute(), `is`(empty()))
        assertThat(child.parent.getOrCompute(), `is`(nullValue()))
    }
}