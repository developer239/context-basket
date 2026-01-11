package com.example.contextbasket.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.Topic

/**
 * Listener interface for basket changes.
 */
interface BasketChangeListener {
    fun onBasketChanged()
}

/**
 * Project-scoped service that holds the selected files in memory.
 * No persistence - resets when project closes.
 */
@Service(Service.Level.PROJECT)
class BasketService(private val project: Project) {

    companion object {
        val BASKET_TOPIC: Topic<BasketChangeListener> = Topic.create(
            "ContextBasketChanges",
            BasketChangeListener::class.java
        )

        fun getInstance(project: Project): BasketService {
            return project.getService(BasketService::class.java)
        }
    }

    private val selectedFiles: MutableSet<VirtualFile> = mutableSetOf()

    /**
     * Add files to the basket. Folders are processed recursively.
     */
    fun addFiles(files: Collection<VirtualFile>) {
        files.forEach { file ->
            addFileRecursively(file)
        }
        notifyListeners()
    }

    /**
     * Remove files from the basket. Folders are processed recursively.
     */
    fun removeFiles(files: Collection<VirtualFile>) {
        files.forEach { file ->
            removeFileRecursively(file)
        }
        notifyListeners()
    }

    /**
     * Remove a single file from the basket.
     */
    fun removeFile(file: VirtualFile) {
        selectedFiles.remove(file)
        notifyListeners()
    }

    /**
     * Clear all files from the basket.
     */
    fun clearAll() {
        selectedFiles.clear()
        notifyListeners()
    }

    /**
     * Get all files currently in the basket.
     */
    fun getFiles(): Set<VirtualFile> = selectedFiles.toSet()

    /**
     * Check if a file is in the basket.
     */
    fun containsFile(file: VirtualFile): Boolean = selectedFiles.contains(file)

    /**
     * Check if the basket is empty.
     */
    fun isEmpty(): Boolean = selectedFiles.isEmpty()

    private fun addFileRecursively(file: VirtualFile) {
        if (file.isDirectory) {
            file.children.forEach { child ->
                addFileRecursively(child)
            }
        } else {
            selectedFiles.add(file)
        }
    }

    private fun removeFileRecursively(file: VirtualFile) {
        if (file.isDirectory) {
            file.children.forEach { child ->
                removeFileRecursively(child)
            }
        } else {
            selectedFiles.remove(file)
        }
    }

    private fun notifyListeners() {
        project.messageBus.syncPublisher(BASKET_TOPIC).onBasketChanged()
    }
}
