package com.example.contextbasket.ui

import com.example.contextbasket.service.BasketChangeListener
import com.example.contextbasket.service.BasketService
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * Main panel for the Context Basket tool window.
 * Shows list of files with remove buttons and action buttons at the bottom.
 */
class BasketToolWindowPanel(private val project: Project) : JBPanel<BasketToolWindowPanel>(BorderLayout()) {

    private val basketService = BasketService.getInstance(project)
    private val listModel = DefaultListModel<VirtualFile>()
    private val fileList = JBList(listModel)

    init {
        setupUI()
        setupMessageBus()
        refreshList()
    }

    private fun setupUI() {
        // File list with custom renderer
        fileList.cellRenderer = FileListCellRenderer(project)
        fileList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // Double-click to open file
        fileList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val selectedFile = fileList.selectedValue
                    if (selectedFile != null) {
                        FileEditorManager.getInstance(project).openFile(selectedFile, true)
                    }
                }
            }
        })

        // Scroll pane for the list
        val scrollPane = JBScrollPane(fileList)

        // Button panel
        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(JButton("Remove").apply {
                addActionListener { removeSelectedFile() }
            })
            add(Box.createHorizontalStrut(5))
            add(JButton("Clear All").apply {
                addActionListener { clearAll() }
            })
            add(Box.createHorizontalGlue())
            add(JButton("Copy to Clipboard").apply {
                addActionListener { copyToClipboard() }
            })
        }

        // Layout
        add(scrollPane, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun setupMessageBus() {
        project.messageBus.connect().subscribe(
            BasketService.BASKET_TOPIC,
            object : BasketChangeListener {
                override fun onBasketChanged() {
                    ApplicationManager.getApplication().invokeLater {
                        refreshList()
                    }
                }
            }
        )
    }

    private fun refreshList() {
        listModel.clear()
        basketService.getFiles()
            .sortedBy { it.path }
            .forEach { listModel.addElement(it) }
    }

    private fun removeSelectedFile() {
        val selectedFile = fileList.selectedValue ?: return
        basketService.removeFile(selectedFile)
        ProjectView.getInstance(project).refresh()
    }

    private fun clearAll() {
        basketService.clearAll()
        ProjectView.getInstance(project).refresh()
    }

    private fun copyToClipboard() {
        val files = basketService.getFiles()
        if (files.isEmpty()) return

        val content = buildString {
            files.sortedBy { it.path }.forEach { file ->
                appendLine("==== ${file.path} ====")
                try {
                    appendLine(String(file.contentsToByteArray()))
                } catch (e: Exception) {
                    appendLine("// Error reading file: ${e.message}")
                }
                appendLine()
            }
        }

        val selection = StringSelection(content)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
    }
}

/**
 * Custom cell renderer to show file name with path tooltip.
 */
private class FileListCellRenderer(private val project: Project) : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): java.awt.Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        val file = value as? VirtualFile
        if (file != null) {
            // Show relative path from project root
            val projectPath = project.basePath ?: ""
            val relativePath = file.path.removePrefix(projectPath).removePrefix("/")
            text = relativePath
            toolTipText = file.path
        }

        return this
    }
}
