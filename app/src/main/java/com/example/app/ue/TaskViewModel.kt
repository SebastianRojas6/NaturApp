package com.example.app.ue

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.Task
import com.example.app.data.TaskDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TaskDatabase.getInstance(application).taskDao()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            dao.findAll().collect { _tasks.value = it }
        }
    }

    fun addTask(description: String) {
        if (description.isBlank()) return
        viewModelScope.launch {
            dao.createOne(Task(description = description))
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            dao.deleteOne(id)
        }
    }

    fun toggleCompleted(id: Int, current: Boolean) {
        viewModelScope.launch {
            dao.toggleIsCompleted(id, !current)
        }
    }

    fun setQuery(text: String) {
        _query.value = text
    }
}