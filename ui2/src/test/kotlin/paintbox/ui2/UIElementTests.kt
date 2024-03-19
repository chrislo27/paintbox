package paintbox.ui2

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import paintbox.ui2.defaultimpl.DefaultParent
import paintbox.ui2.defaultimpl.DefaultUIElement
import kotlin.test.Test


class UIElementTests {
    
    @Test
    fun `adding an element as a child sets its parent property`() {
        // Arrange
        val parent = DefaultParent()
        val child = DefaultUIElement()
        
        // Act
        parent.addChild(child)
        
        // Assert
        assertThat(parent.children.getOrCompute(), contains(child))
        assertThat(child.parent.getOrCompute(), `is`(parent))
    }
    
    @Test
    fun `removing an element as a child nulls its parent property`() {
        // Arrange
        val parent = DefaultParent()
        val child = DefaultUIElement()
        
        parent.addChild(child)
        
        // Act
        parent.removeChild(child)
        
        // Assert
        assertThat(parent.children.getOrCompute(), `is`(empty()))
        assertThat(child.parent.getOrCompute(), `is`(nullValue()))
    }
}