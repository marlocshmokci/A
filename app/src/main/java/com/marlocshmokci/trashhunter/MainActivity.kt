package com.marlocshmokci.trashhunter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TrashHunterApp() }
    }
}

@Composable
private fun TrashHunterApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTree by remember { mutableStateOf<Uri?>(null) }
    var scanning by remember { mutableStateOf(false) }
    var files by remember { mutableStateOf(emptyList<FileItem>()) }
    var status by remember { mutableStateOf("Выбери папку для анализа") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            selectedTree = uri
            status = "Папка выбрана. Нажми «Сканировать»."
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF5CE1A5),
            background = Color(0xFF111318),
            surface = Color(0xFF191C22)
        )
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    listOf("Обзор", "Найдено", "Настройки").forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {},
                            label = { Text(title) }
                        )
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> HomeScreen(
                    padding = padding,
                    selectedTree = selectedTree,
                    scanning = scanning,
                    status = status,
                    onPick = { picker.launch(null) },
                    onScan = {
                        val tree = selectedTree ?: return@HomeScreen
                        scanning = true
                        status = "Сканирование…"
                        files = FileScanner.scan(context, tree)
                        scanning = false
                        status = "Сканирование завершено: " + files.size + " находок"
                    }
                )
                1 -> ResultsScreen(
                    padding = padding,
                    files = files,
                    onDelete = { item ->
                        if (FileScanner.delete(context, item)) {
                            files = files.filterNot { it.uri == item.uri }
                        }
                    }
                )
                else -> SettingsScreen(padding)
            }
        }
    }
}

@Composable
private fun HomeScreen(
    padding: PaddingValues,
    selectedTree: Uri?,
    scanning: Boolean,
    status: String,
    onPick: () -> Unit,
    onScan: () -> Unit
) {
    val context = LocalContext.current
    val folderName = selectedTree?.let { DocumentFile.fromTreeUri(context, it)?.name }
        ?: "Папка не выбрана"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("TrashHunter", style = MaterialTheme.typography.headlineLarge)
            Text("Умный анализатор хранилища", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Источник", style = MaterialTheme.typography.titleMedium)
                    Text(folderName)
                    OutlinedButton(onClick = onPick, modifier = Modifier.fillMaxWidth()) {
                        Text("Выбрать папку")
                    }
                    Button(
                        onClick = onScan,
                        enabled = selectedTree != null && !scanning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (scanning) "Сканирование…" else "Начать сканирование")
                    }
                    Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text("Как определяется мусор", style = MaterialTheme.typography.titleLarge)
            Text(
                "Временные расширения получают высокий приоритет. APK, архивы и очень большие файлы " +
                    "попадают на ручную проверку. Важные файлы автоматически не удаляются."
            )
        }
    }
}

@Composable
private fun ResultsScreen(
    padding: PaddingValues,
    files: List<FileItem>,
    onDelete: (FileItem) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<FileItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Найдено", style = MaterialTheme.typography.headlineMedium) }
        if (files.isEmpty()) {
            item { Text("Пока ничего не найдено. Выбери папку и запусти сканирование.") }
        } else {
            items(files, key = { it.uri.toString() }) { item ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(formatBytes(item.size), color = MaterialTheme.colorScheme.primary)
                        Text(item.reason, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Уверенность: " + item.confidence + "% • " + item.category)
                        if (item.category == Category.JUNK) {
                            FilledTonalButton(onClick = { pendingDelete = item }) {
                                Text("Удалить")
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Удалить файл?") },
            text = { Text(item.name + "

Освободится примерно " + formatBytes(item.size) + ".") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(item)
                    pendingDelete = null
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SettingsScreen(padding: PaddingValues) {
    Column(
        Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineMedium)
        Text("Безопасный режим: включён")
        Text("Удаление требует подтверждения пользователя.")
        Text("Сканируются только папки, которым пользователь выдал доступ.")
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return bytes.toString() + " B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var i = 0
    while (value >= 1024 && i < units.lastIndex) {
        value /= 1024
        i++
    }
    return String.format(Locale.US, "%.1f %s", value, units[i])
}
