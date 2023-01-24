package paintbox.filechooser

import java.awt.Component
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter


/**
 * Implementation of [IFileDialog] using Swing's [JFileChooser].
 */
open class AWTFileDialog : IFileDialog {

    companion object {
        init {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected open fun createJFileChooser(currentDir: File?): JFileChooser {
        return object : JFileChooser(currentDir) {
            override fun createDialog(parent: Component?): JDialog {
                return super.createDialog(parent).apply {
                    setIconImage(BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB))
                }
            }
        }
    }

    protected fun File.parentOrSelf(): File {
        return if (this.isFile) this.parentFile else this
    }

    protected fun FileExtFilter.toFNEF(): FileNameExtensionFilter {
        val filter = this
        return FileNameExtensionFilter(filter.description, *filter.extensions.toTypedArray())
    }

    override fun saveFile(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (File?) -> Unit) {
        val fc = createJFileChooser(defaultFile?.parentOrSelf()).apply {
            this.fileSelectionMode = JFileChooser.FILES_ONLY
            this.dialogTitle = title
        }
        if (filter != null) {
            fc.fileFilter = filter.toFNEF()
        }

        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            callback(fc.selectedFile)
        } else {
            callback(null)
        }
    }

    override fun openFile(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (File?) -> Unit) {
        val fc = createJFileChooser(defaultFile?.parentOrSelf()).apply {
            this.fileSelectionMode = JFileChooser.FILES_ONLY
            this.dialogTitle = title
        }
        if (filter != null) {
            fc.fileFilter = filter.toFNEF()
        }

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            callback(fc.selectedFile)
        } else {
            callback(null)
        }
    }

    override fun openMultipleFiles(
        title: String,
        defaultFile: File?,
        filter: FileExtFilter?,
        callback: (List<File>) -> Unit,
    ) {
        val fc = createJFileChooser(defaultFile?.parentOrSelf()).apply {
            this.fileSelectionMode = JFileChooser.FILES_ONLY
            this.isMultiSelectionEnabled = true
            this.dialogTitle = title
        }
        if (filter != null) {
            fc.fileFilter = filter.toFNEF()
        }

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            callback(fc.selectedFiles?.toList() ?: emptyList())
        } else {
            callback(emptyList())
        }
    }

    override fun selectFolder(title: String, defaultDir: File?, callback: (File?) -> Unit) {
        val fc = createJFileChooser(defaultDir?.parentOrSelf()).apply {
            this.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            this.dialogTitle = title
        }

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            callback(fc.selectedFile)
        } else {
            callback(null)
        }
    }
}