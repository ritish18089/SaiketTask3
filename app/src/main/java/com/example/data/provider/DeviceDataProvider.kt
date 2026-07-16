package com.example.data.provider

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import com.example.data.models.CallHistory
import com.example.data.models.CallType
import com.example.data.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DeviceDataProvider {
    suspend fun getContacts(context: Context): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (it.moveToNext()) {
                    val name = if (nameIndex != -1) it.getString(nameIndex) ?: "" else ""
                    val number = if (numberIndex != -1) it.getString(numberIndex) ?: "" else ""
                    val normalizedNumber = number.replace(Regex("[^0-9+]"), "")
                    if (normalizedNumber.isNotEmpty()) {
                        contacts.add(Contact(name = name, phoneNumber = normalizedNumber))
                    }
                }
            }
        } catch (e: Exception) {}
        contacts.distinctBy { it.phoneNumber }
    }

    suspend fun getCallLog(context: Context): List<CallHistory> = withContext(Dispatchers.IO) {
        val history = mutableListOf<CallHistory>()
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null, null, CallLog.Calls.DATE + " DESC"
            )

            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext()) {
                    val number = if (numberIndex != -1) it.getString(numberIndex) ?: "" else ""
                    val name = if (nameIndex != -1) it.getString(nameIndex) else null
                    val typeInt = if (typeIndex != -1) it.getInt(typeIndex) else 0
                    val date = if (dateIndex != -1) it.getLong(dateIndex) else 0L
                    val duration = if (durationIndex != -1) it.getInt(durationIndex) else 0

                    val callType = when (typeInt) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        else -> CallType.INCOMING
                    }

                    history.add(CallHistory(
                        contactName = name,
                        phoneNumber = number,
                        callType = callType,
                        timestamp = date,
                        durationSeconds = duration
                    ))
                }
            }
        } catch (e: Exception) {}
        history
    }
}
