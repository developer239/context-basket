package com.example.contextbasket.ui

import com.example.contextbasket.service.BasketChangeListener
import com.example.contextbasket.service.BasketService
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import java.awt.Color

/**
 * Highlights files in the Project View that are in the Context Basket.
 */
class ProjectViewHighlighter : TreeStructureProvider {

    // Highlight color - light yellow/green
    private val highlightColor = JBColor(
        Color(220, 255, 220),  // Light green for light theme
        Color(50, 80, 50)      // Dark green for dark theme
    )

    override fun modify(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings?
    ): Collection<AbstractTreeNode<*>> {
        val project = parent.project ?: return children

        // Subscribe to basket changes to refresh view (only once per project)
        subscribeToBasketChanges(project)

        val basketService = BasketService.getInstance(project)
        if (basketService.isEmpty()) return children

        return children.map { node ->
            if (node is PsiFileNode) {
                val virtualFile = node.virtualFile
                if (virtualFile != null && basketService.containsFile(virtualFile)) {
                    HighlightedFileNode(node, highlightColor)
                } else {
                    node
                }
            } else {
                node
            }
        }
    }

    private val subscribedProjects = mutableSetOf<Project>()

    private fun subscribeToBasketChanges(project: Project) {
        if (project in subscribedProjects) return
        subscribedProjects.add(project)

        project.messageBus.connect().subscribe(
            BasketService.BASKET_TOPIC,
            object : BasketChangeListener {
                override fun onBasketChanged() {
                    ApplicationManager.getApplication().invokeLater {
                        ProjectView.getInstance(project).refresh()
                    }
                }
            }
        )
    }
}

/**
 * Wrapper node that adds highlighting to a file node.
 */
private class HighlightedFileNode(
    private val originalNode: PsiFileNode,
    private val highlightColor: Color
) : ProjectViewNode<Any>(originalNode.project, originalNode.value, originalNode.settings) {

    override fun contains(file: VirtualFile): Boolean = originalNode.contains(file)

    override fun getChildren(): Collection<AbstractTreeNode<*>> = originalNode.children

    override fun update(presentation: PresentationData) {
        originalNode.update(presentation)

        val fileName = originalNode.virtualFile?.name ?: return

        presentation.clearText()
        presentation.addText(
            fileName,
            SimpleTextAttributes(
                SimpleTextAttributes.STYLE_PLAIN,
                null,
                highlightColor
            )
        )
    }
}
