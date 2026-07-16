package com.example.data.repository

import com.example.data.db.CallHistoryDao
import com.example.data.models.CallHistory
import kotlinx.coroutines.flow.Flow

class CallHistoryRepository(private val callHistoryDao: CallHistoryDao) {
    val allCallHistory: Flow<List<CallHistory>> = callHistoryDao.getAllCallHistory()

    suspend fun insert(callHistory: CallHistory) = callHistoryDao.insertCallHistory(callHistory)
    suspend fun clearHistory() = callHistoryDao.clearCallHistory()
}
