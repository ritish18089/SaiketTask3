package com.example.data.repository

import com.example.data.db.ContactDao
import com.example.data.models.Contact
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val favoriteContacts: Flow<List<Contact>> = contactDao.getFavoriteContacts()

    suspend fun insert(contact: Contact) = contactDao.insertContact(contact)
    suspend fun insertIgnore(contact: Contact) = contactDao.insertIgnoreContact(contact)
    suspend fun update(contact: Contact) = contactDao.updateContact(contact)
    suspend fun deleteById(id: Int) = contactDao.deleteContactById(id)
    fun searchContacts(query: String): Flow<List<Contact>> = contactDao.searchContacts(query)
}
