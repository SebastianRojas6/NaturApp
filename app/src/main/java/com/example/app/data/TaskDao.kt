package com.example.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun findAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE description LIKE '%' || :text || '%'")
    fun searchByDescription(text: String): Flow<List<Task>>

    @Insert
    suspend fun createOne(task: Task)

    @Query("UPDATE tasks SET isCompleted = :newState WHERE id = :id")
    suspend fun toggleIsCompleted(id: Int, newState: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteOne(id: Int)
}