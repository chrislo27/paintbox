package paintbox.filechooser

import com.badlogic.gdx.graphics.Color
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil.memAddress
import org.lwjgl.util.tinyfd.TinyFileDialogs.*
import paintbox.util.SystemUtils
import java.awt.Component
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.UIManager


object TinyFDWrapper : IFileDialog {
    
    private val isWindows: Boolean = SystemUtils.isWindows()

    private fun File?.toProperPath(): String? {
        if (this == null) return null
        if (this.isDirectory) return this.absolutePath + "/"
        return this.absolutePath
    }
    
    private fun String.starDot(): String {
        if (this.startsWith("*.")) return this
        return "*.$this"
    }

    private fun openFile(title: String, defaultFile: String?, filter: FileExtFilter?): File? {
        return if (filter == null) {
            val path = tinyfd_openFileDialog(title, defaultFile, null, null, false) ?: return null
            File(path)
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(memAddress(stack.UTF8(it.starDot())))
                }
                filterPatterns.flip()

                val path = tinyfd_openFileDialog(title, defaultFile, filterPatterns, filter.description, false)
                        ?: return null
                File(path)
            }
        }
    }

    override fun openFile(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (File?) -> Unit) {
        openFile(title, defaultFile.toProperPath(), filter).let(callback)
    }

    private fun openMultipleFiles(title: String, defaultFile: String?, filter: FileExtFilter?): List<File> {
        return if (filter == null) {
            val path = tinyfd_openFileDialog(title, defaultFile, null, null, true) ?: return emptyList()
            path.split('|').map { File(it) }
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(memAddress(stack.UTF8(it.starDot())))
                }
                filterPatterns.flip()

                val path = tinyfd_openFileDialog(title, defaultFile, filterPatterns, filter.description, true)
                        ?: return emptyList()
                path.split('|').map { File(it) }
            }
        }
    }

    /**
     * Opens an open file chooser dialog that can select multiple files.
     * [callback] is called when the dialog is closed/file(s) is selected.
     */
    override fun openMultipleFiles(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (List<File>) -> Unit) {
        openMultipleFiles(title, defaultFile.toProperPath(), filter).let(callback)
    }

    private fun saveFile(title: String, defaultFile: String?, filter: FileExtFilter?): File? {
        return if (filter == null) {
            val path = tinyfd_saveFileDialog(title, defaultFile, null, null) ?: return null
            File(path)
        } else {
            val stack: MemoryStack = MemoryStack.stackPush()
            stack.use {
                val filterPatterns: PointerBuffer = stack.mallocPointer(filter.extensions.size)
                filter.extensions.forEach {
                    filterPatterns.put(memAddress(stack.UTF8(it.starDot())))
                }
                filterPatterns.flip()

                val path = tinyfd_saveFileDialog(title, defaultFile, filterPatterns, filter.description) ?: return null
                File(path)
            }
        }
    }

    /**
     * Opens a save file chooser dialog.
     * [callback] is called when the dialog is closed/a file is selected.
     */
    override fun saveFile(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (File?) -> Unit) {
        saveFile(title, defaultFile.toProperPath(), filter).let(callback)
    }

    private fun selectFolder(title: String, defaultFolder: String?): File? {
        if (isWindows) {
            // The Windows native folder select dialog doesn't behave well with respect to cancel,
            // specifically it returns the last selected folder (???)
            // So this version will use Swing and a JFileChooser.
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val fileChooser = object : JFileChooser(defaultFolder) {
                override fun createDialog(parent: Component?): JDialog {
                    return super.createDialog(parent).apply { 
                        setIconImage(BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB))
                    }
                }
            }.apply { 
                this.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                this.dialogTitle = title
            }
            return when (fileChooser.showOpenDialog(null)) {
                JFileChooser.APPROVE_OPTION -> {
                    fileChooser.selectedFile
                }
                else -> null
            }
        } else {
            val path = tinyfd_selectFolderDialog(title, defaultFolder ?: "") ?: return null
            return File(path)
        }
    }

    /**
     * Opens a select folder/directory chooser dialog.
     * [callback] is called when the dialog is closed/a directory is selected.
     * 
     * Broken on Windows: returns the last directory if the user hits CANCEL, instead of returning null
     */
    override fun selectFolder(title: String, defaultDir: File?, callback: (File?) -> Unit) {
        selectFolder(title, defaultDir.toProperPath()).let(callback)
    }

    /**
     * Opens a colour selection dialog. Returns null if the chooser was cancelled.
     */
    fun selectColor(title: String, defaultHexColor: String): Color? {
        val stack: MemoryStack = MemoryStack.stackPush()
        stack.use {
            val color: ByteBuffer = stack.malloc(3)
            val hex: String = tinyfd_colorChooser(title, defaultHexColor, null, color) ?: return null
            return Color.valueOf(hex).apply { a = 1f }
        }
    }

    /**
     * Opens a colour selection dialog. Returns null if the chooser was cancelled.
     */
    fun selectColor(title: String, defaultColor: Color?): Color? {
        val stack: MemoryStack = MemoryStack.stackPush()
        stack.use {
            val color: ByteBuffer = stack.malloc(3)
            val def = if (defaultColor == null) "#FFFFFF" else "#${defaultColor.toString().take(6)}"
            val hex: String = tinyfd_colorChooser(title, def, null, color) ?: return null
            return Color.valueOf(hex).apply { a = 1f }
        }
    }

}
