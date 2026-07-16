package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts", indices = [androidx.room.Index(value = ["phoneNumber"], unique = true)])
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val isFavorite: Boolean = false,
    val avatarUrl: String? = null
)
