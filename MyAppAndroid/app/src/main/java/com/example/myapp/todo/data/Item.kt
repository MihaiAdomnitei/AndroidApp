package com.example.myapp.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "Products")
data class Item(
    @PrimaryKey val _id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val price: Int = 0,
    val date: String = "",
    val sold: Boolean = false,
    val isSynced: Boolean = true  // false = needs to be synced with server
)
