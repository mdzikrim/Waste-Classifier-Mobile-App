package com.example.tes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SampahEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sampahDao(): SampahDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sampah_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
