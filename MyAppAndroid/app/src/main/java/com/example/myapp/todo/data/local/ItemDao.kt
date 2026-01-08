package com.example.myapp.todo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapp.todo.data.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM Products")
    fun getAll(): Flow<List<Item>>

    @Query("SELECT * FROM Products WHERE _id = :id")
    suspend fun getById(id: String): Item?

    @Query("SELECT * FROM Products WHERE isSynced = 0")
    suspend fun getUnsyncedItems(): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Query("DELETE FROM Products")
    suspend fun deleteAll()

    @Query("DELETE FROM Products WHERE _id = :id")
    suspend fun deleteById(id: String)
}

