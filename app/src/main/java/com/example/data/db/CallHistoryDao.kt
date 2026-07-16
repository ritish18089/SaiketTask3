package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.models.CallHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getAllCallHistory(): Flow<List<CallHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallHistory(callHistory: CallHistory)

    @Query("DELETE FROM call_history")
    suspend fun clearCallHistory()
}
