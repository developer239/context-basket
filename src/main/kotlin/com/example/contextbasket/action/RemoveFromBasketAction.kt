package com.example.contextbasket.action

import com.example.contextbasket.service.BasketService
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile

/**
 * Action to remove selected files/folders from the Context Basket.
 * Only visible when selected files are in the basket.
 */
class RemoveFromBasketAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        val basketService = BasketService.getInstance(project)
        basketService.removeFiles(files.toList())

        // Refresh project view to update highlighting
        ProjectView.getInstance(project).refresh()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)

        // Only show if at least one selected file is in the basket
        e.presentation.isEnabledAndVisible = when {
            project == null -> false
            files.isNullOrEmpty() -> false
            else -> {
                val basketService = BasketService.getInstance(project)
                files.any { file -> isFileInBasket(file, basketService) }
            }
        }
    }

    /**
     * Check if file (or any child if directory) is in the basket.
     */
    private fun isFileInBasket(file: VirtualFile, basketService: BasketService): Boolean {
        return if (file.isDirectory) {
            file.children.any { child -> isFileInBasket(child, basketService) }
        } else {
            basketService.containsFile(file)
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
