package com.example.myapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapp.todo.data.Item
import com.example.myapp.todo.data.local.ItemDao

@Database(entities = arrayOf(Item::class), version = 5)
abstract class MyAppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: MyAppDatabase? = null

        fun getDatabase(context: Context): MyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    MyAppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
