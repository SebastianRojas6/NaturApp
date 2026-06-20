package com.example.app.ue

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app.data.Task
import kotlin.math.roundToInt

@Composable
fun TodoScreen(vm: TaskViewModel = viewModel()) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }

    val filtered = if (query.isBlank()) tasks
    else tasks.filter { it.description.contains(query, ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFDADDE4),
                        Color(0xFFC0D3FD),
                        Color(0xFF76A9D5),
                        Color(0xFF512DA8)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Agregar o Buscar tareas") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            vm.setQuery(text)
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Buscar")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (query.isBlank()) {
                    Button(
                        onClick = { vm.addTask(text); text = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Agregar tarea") }
                } else {
                    Button(
                        onClick = { vm.setQuery(""); text = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Ver Todo") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(filtered, key = { it.id }) { task ->
                        TodoItem(
                            task = task,
                            onDelete = { vm.deleteTask(task.id) },
                            onToggle = { vm.toggleCompleted(task.id, task.isCompleted) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF512DA8))
    ) {
        Text(
            text = "tareApps",
            color = Color(0xFFF2E9E9),
            fontSize = 28.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 12.dp)
        )
    }
}

@Composable
fun TodoItem(task: Task, onDelete: () -> Unit, onToggle: () -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val swipeThreshold = 200f

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar tarea") },
            text = { Text("¿Estás seguro de que deseas eliminar esta tarea?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    val redAlpha = if (offsetX < 0) (-offsetX / swipeThreshold).coerceIn(0f, 1f) else 0f
    val purpleAlpha = if (offsetX > 0) (offsetX / swipeThreshold).coerceIn(0f, 1f) else 0f

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Red.copy(alpha = redAlpha)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = redAlpha))
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF8A2BE2).copy(alpha = purpleAlpha)),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White.copy(alpha = purpleAlpha))
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEDDDF5))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX < -swipeThreshold -> showDeleteDialog = true
                                offsetX > swipeThreshold -> onToggle()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount -> offsetX += dragAmount }
                    )
                }
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (task.description.length > 35)
                        task.description.take(35) + "..." else task.description,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) Color(0xFF512DA8) else Color.Black,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}