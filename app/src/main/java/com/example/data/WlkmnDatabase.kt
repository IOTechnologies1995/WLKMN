package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackEntity::class], version = 1, exportSchema = false)
abstract class WlkmnDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: WlkmnDatabase? = null

        fun getInstance(context: Context): WlkmnDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WlkmnDatabase::class.java,
                    "wlkmn_library.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
