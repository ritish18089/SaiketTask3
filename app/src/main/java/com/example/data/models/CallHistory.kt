package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallType {
    INCOMING, OUTGOING, MISSED
}

@Entity(tableName = "call_history")
data class CallHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String?,
    val phoneNumber: String,
    val callType: CallType,
    val timestamp: Long,
    val durationSeconds: Int
)
