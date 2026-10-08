package com.marlocshmokci.trashhunter

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

data class FileItem(
    val name: String,
    val uri: Uri,
    val size: Long,
    val reason: String,
    val confidence: Int,
    val category: Category
)

enum class Category { JUNK, REVIEW, KEEP }

object FileScanner {
    private val junkExtensions = setOf("tmp", "temp", "log", "bak", "old", "cache", "dmp")
    private val reviewExtensions = setOf("apk", "zip", "rar", "7z", "iso")

    fun scan(context: Context, treeUri: Uri): List<FileItem> {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val all = mutableListOf<FileItem>()
        scanDir(root, all)
        return all.sortedByDescending { it.size }
    }

    private fun scanDir(dir: DocumentFile, out: MutableList<FileItem>) {
        for (file in dir.listFiles()) {
            if (file.isDirectory) {
                scanDir(file, out)
                continue
            }

            val name = file.name ?: continue
            val size = file.length()
            val ext = name.substringAfterLast('.', "").lowercase()

            val result = when {
                ext in junkExtensions ->
                    Triple("Похож на временный или служебный файл", 92, Category.JUNK)
                ext in reviewExtensions ->
                    Triple("Архив или установочный файл — проверь перед удалением", 58, Category.REVIEW)
                size >= 500L * 1024L * 1024L ->
                    Triple("Очень большой файл — проверь, нужен ли он", 74, Category.REVIEW)
                name.contains("copy", true) || name.contains("duplicate", true) ->
                    Triple("Имя похоже на копию файла", 80, Category.REVIEW)
                else -> null
            }

            if (result != null) {
                out += FileItem(name, file.uri, size, result.first, result.second, result.third)
            }
        }
    }

    fun delete(context: Context, item: FileItem): Boolean {
        return DocumentFile.fromSingleUri(context, item.uri)?.delete() == true
    }
}
