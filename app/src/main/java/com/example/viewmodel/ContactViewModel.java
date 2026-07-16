package com.example.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.data.models.BlockedNumber;
import com.example.data.models.CallLogEntry;
import com.example.model.Contact;
import com.example.repository.ContactRepository;
import java.util.List;

public class ContactViewModel extends AndroidViewModel {
    private final ContactRepository repository;
    private final LiveData<List<Contact>> allContacts;
    private final LiveData<List<Contact>> favoriteContacts;
    private final LiveData<List<CallLogEntry>> callHistory;
    private final LiveData<List<BlockedNumber>> blockedNumbers;
    private final MutableLiveData<String> updateStatus = new MutableLiveData<>();

    public ContactViewModel(@NonNull Application application) {
        super(application);
        repository = new ContactRepository(application);
        allContacts = repository.getAllContacts();
        favoriteContacts = repository.getFavoriteContacts();
        callHistory = repository.getCallHistory();
        blockedNumbers = repository.getBlockedNumbers();
    }

    public LiveData<List<Contact>> getAllContacts() { return allContacts; }
    public LiveData<List<Contact>> getFavoriteContacts() { return favoriteContacts; }
    public LiveData<List<CallLogEntry>> getCallHistory() { return callHistory; }
    public LiveData<List<BlockedNumber>> getBlockedNumbers() { return blockedNumbers; }
    public LiveData<String> getUpdateStatus() { return updateStatus; }

    public void updateContact(Contact contact) {
        repository.updateContact(contact, new ContactRepository.UpdateCallback() {
            @Override
            public void onSuccess() { updateStatus.postValue("SUCCESS"); }
            @Override
            public void onError(String error) { updateStatus.postValue(error); }
        });
    }

    public void blockNumber(String number, String label) {
        repository.blockNumber(number, label);
    }

    public void unblockNumber(BlockedNumber blockedNumber) {
        repository.unblockNumber(blockedNumber);
    }

    public void clearCallHistory() {
        repository.clearCallHistory();
    }

    public void clearCallHistoryForContact(java.util.List<String> numbers) {
        repository.clearCallHistoryForContact(numbers, new ContactRepository.UpdateCallback() {
            @Override
            public void onSuccess() { updateStatus.postValue("HISTORY_CLEARED"); }
            @Override
            public void onError(String error) { updateStatus.postValue(error); }
        });
    }

    public void deleteContact(String contactId) {
        repository.deleteContact(contactId, new ContactRepository.UpdateCallback() {
            @Override
            public void onSuccess() { updateStatus.postValue("DELETED"); }
            @Override
            public void onError(String error) { updateStatus.postValue(error); }
        });
    }

    public LiveData<List<CallLogEntry>> getCallHistoryForContact(java.util.List<String> numbers) {
        return repository.getCallHistoryForContact(numbers);
    }
}
