package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.models.CallHistory
import com.example.data.models.CallType
import com.example.data.models.Contact
import com.example.data.repository.CallHistoryRepository
import com.example.data.repository.ContactRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.data.provider.DeviceDataProvider
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val contactRepository = ContactRepository(database.contactDao())
    private val callHistoryRepository = CallHistoryRepository(database.callHistoryDao())

    val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val contacts: StateFlow<List<Contact>> = searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                contactRepository.allContacts
            } else {
                contactRepository.searchContacts(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteContacts: StateFlow<List<Contact>> = contactRepository.favoriteContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callHistory: StateFlow<List<CallHistory>> = callHistoryRepository.allCallHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addContact(name: String, phoneNumber: String, isFavorite: Boolean = false) {
        viewModelScope.launch {
            contactRepository.insert(Contact(name = name, phoneNumber = phoneNumber, isFavorite = isFavorite))
        }
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            contactRepository.update(contact.copy(isFavorite = !contact.isFavorite))
        }
    }

    fun makeCall(contactName: String?, phoneNumber: String) {
        // Mock a call by adding to call history
        viewModelScope.launch {
            val history = CallHistory(
                contactName = contactName,
                phoneNumber = phoneNumber,
                callType = CallType.OUTGOING,
                timestamp = System.currentTimeMillis(),
                durationSeconds = (1..300).random()
            )
            callHistoryRepository.insert(history)
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            callHistoryRepository.clearHistory()
        }
    }

    fun syncDeviceData(context: Context) {
        viewModelScope.launch {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                val deviceContacts = DeviceDataProvider.getContacts(context)
                deviceContacts.forEach { contactRepository.insertIgnore(it) }
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                val deviceLogs = DeviceDataProvider.getCallLog(context)
                callHistoryRepository.clearHistory()
                deviceLogs.forEach { callHistoryRepository.insert(it) }
            }
        }
    }

    fun makeActualCall(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent)
            makeCall(null, phoneNumber) // Mock entry for rapid UI update, though device sync will catch it later
        } else {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
}
