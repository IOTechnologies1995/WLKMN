package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uriString: String,
    val rawResName: String? = null,
    val trackNumber: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
    val isDemo: Boolean = false
)
