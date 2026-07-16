package com.example.repository;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.db.AppDatabaseJava;
import com.example.data.db.BlockedNumberDao;
import com.example.data.models.BlockedNumber;
import com.example.data.models.CallLogEntry;
import com.example.model.Contact;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactRepository {
    private static final String TAG = "ContactRepository";
    private final ContentResolver contentResolver;
    private final Resources resources;
    private final ExecutorService executorService;
    private final Context appContext;
    private final BlockedNumberDao blockedNumberDao;
    private final MutableLiveData<List<Contact>> allContactsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Contact>> favoriteContactsLiveData = new MutableLiveData<>();

    public ContactRepository(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        this.contentResolver = appContext.getContentResolver();
        this.resources = appContext.getResources();
        this.executorService = Executors.newFixedThreadPool(4);
        this.blockedNumberDao = AppDatabaseJava.getDatabase(appContext).blockedNumberDao();
        refreshContacts();
    }

    public void refreshContacts() {
        executorService.execute(() -> {
            List<Contact> contactList = new ArrayList<>();
            List<Contact> favoritesList = new ArrayList<>();
            try (Cursor cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Contact contact = mapCursorToContact(cursor);
                        if (contact != null) {
                            loadContactDetails(contact);
                            contactList.add(contact);
                            if (contact.isFavorite()) {
                                favoritesList.add(contact);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying contacts", e);
            }
            allContactsLiveData.postValue(contactList);
            favoriteContactsLiveData.postValue(favoritesList);
        });
    }

    public LiveData<List<Contact>> getAllContacts() {
        return allContactsLiveData;
    }

    public LiveData<List<Contact>> getFavoriteContacts() {
        return favoriteContactsLiveData;
    }

    public LiveData<List<CallLogEntry>> getCallHistory() {
        MutableLiveData<List<CallLogEntry>> callHistoryLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<CallLogEntry> history = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                callHistoryLiveData.postValue(history);
                return;
            }
            try (Cursor cursor = contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    null, null, null,
                    CallLog.Calls.DATE + " DESC"
            )) {
                if (cursor != null) {
                    int numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                    int typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE);
                    int durIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);

                    while (cursor.moveToNext()) {
                        history.add(new CallLogEntry(
                                cursor.getString(numIdx),
                                cursor.getString(nameIdx),
                                cursor.getInt(typeIdx),
                                cursor.getLong(dateIdx),
                                cursor.getString(durIdx),
                                null
                        ));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying call log", e);
            }
            callHistoryLiveData.postValue(history);
        });
        return callHistoryLiveData;
    }

    public LiveData<List<BlockedNumber>> getBlockedNumbers() {
        return blockedNumberDao.getAllBlockedNumbers();
    }

    public void blockNumber(String number, String label) {
        executorService.execute(() -> blockedNumberDao.insert(new BlockedNumber(number, label)));
    }

    public void unblockNumber(BlockedNumber blockedNumber) {
        executorService.execute(() -> blockedNumberDao.delete(blockedNumber));
    }

    public void clearCallHistory() {
        executorService.execute(() -> {
            try {
                contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null);
            } catch (Exception e) {
                Log.e(TAG, "Error clearing call log", e);
            }
        });
    }

    public void clearCallHistoryForContact(List<String> numbers, @NonNull UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                if (numbers == null || numbers.isEmpty()) {
                    callback.onSuccess();
                    return;
                }
                StringBuilder selection = new StringBuilder(CallLog.Calls.NUMBER + " IN (");
                List<String> selectionArgs = new ArrayList<>();
                for (int i = 0; i < numbers.size(); i++) {
                    selection.append("?, ?");
                    if (i < numbers.size() - 1) selection.append(", ");
                    String num = numbers.get(i);
                    selectionArgs.add(num);
                    selectionArgs.add(num.replaceAll("[^0-9+]", ""));
                }
                selection.append(")");

                contentResolver.delete(CallLog.Calls.CONTENT_URI, selection.toString(), selectionArgs.toArray(new String[0]));
                callback.onSuccess();
            } catch (SecurityException e) {
                callback.onError("Permission denied to delete call log.");
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void deleteContact(String contactId, @NonNull UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                contentResolver.delete(ContactsContract.RawContacts.CONTENT_URI, 
                        ContactsContract.RawContacts.CONTACT_ID + "=?", new String[]{contactId});
                refreshContacts();
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e != null ? e.getMessage() : "Unknown error");
            }
        });
    }

    public LiveData<List<CallLogEntry>> getCallHistoryForContact(List<String> numbers) {
        MutableLiveData<List<CallLogEntry>> callHistoryLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<CallLogEntry> history = new ArrayList<>();
            if (numbers == null || numbers.isEmpty() || 
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                callHistoryLiveData.postValue(history);
                return;
            }
            try {
                StringBuilder selection = new StringBuilder(CallLog.Calls.NUMBER + " IN (");
                List<String> selectionArgs = new ArrayList<>();
                for (int i = 0; i < numbers.size(); i++) {
                    selection.append("?, ?");
                    if (i < numbers.size() - 1) selection.append(", ");
                    String num = numbers.get(i);
                    selectionArgs.add(num);
                    selectionArgs.add(num.replaceAll("[^0-9+]", ""));
                }
                selection.append(")");

                try (Cursor cursor = contentResolver.query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        selection.toString(),
                        selectionArgs.toArray(new String[0]),
                        CallLog.Calls.DATE + " DESC"
                )) {
                    if (cursor != null) {
                        int numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                        int nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                        int typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE);
                        int dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE);
                        int durIdx = cursor.getColumnIndex(CallLog.Calls.DURATION);

                        while (cursor.moveToNext()) {
                            history.add(new CallLogEntry(
                                    cursor.getString(numIdx),
                                    cursor.getString(nameIdx),
                                    cursor.getInt(typeIdx),
                                    cursor.getLong(dateIdx),
                                    cursor.getString(durIdx),
                                    null
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying contact call log", e);
            }
            callHistoryLiveData.postValue(history);
        });
        return callHistoryLiveData;
    }

    @Nullable
    private Contact mapCursorToContact(@NonNull Cursor cursor) {
        try {
            Contact contact = new Contact();
            int idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int photoIdx = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI);
            int starIdx = cursor.getColumnIndex(ContactsContract.Contacts.STARRED);

            contact.setId(idIdx != -1 ? cursor.getString(idIdx) : "");
            contact.setName(nameIdx != -1 ? cursor.getString(nameIdx) : "Unknown");
            contact.setPhotoUri(photoIdx != -1 ? cursor.getString(photoIdx) : null);
            contact.setFavorite(starIdx != -1 && cursor.getInt(starIdx) == 1);
            return contact;
        } catch (Exception e) {
            return null;
        }
    }

    private void loadContactDetails(@NonNull Contact contact) {
        if (contact.getId() == null) return;
        
        // Load Phones
        try (Cursor phoneCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contact.getId()},
                null
        )) {
            if (phoneCursor != null) {
                int numIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int typeIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                int labelIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
                while (phoneCursor.moveToNext()) {
                    String number = numIdx != -1 ? phoneCursor.getString(numIdx) : null;
                    int type = typeIdx != -1 ? phoneCursor.getInt(typeIdx) : ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                    String customLabel = labelIdx != -1 ? phoneCursor.getString(labelIdx) : null;
                    if (number != null) {
                        contact.getPhoneNumbers().add(new Contact.ContactField(number, getSafeTypeLabel(type, customLabel), type));
                    }
                }
            }
        } catch (Exception ignored) {}

        // Load Emails
        try (Cursor emailCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                new String[]{contact.getId()},
                null
        )) {
            if (emailCursor != null) {
                int addrIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                int typeIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);
                int labelIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL);
                while (emailCursor.moveToNext()) {
                    String address = addrIdx != -1 ? emailCursor.getString(addrIdx) : null;
                    int type = typeIdx != -1 ? emailCursor.getInt(typeIdx) : ContactsContract.CommonDataKinds.Email.TYPE_HOME;
                    String customLabel = labelIdx != -1 ? emailCursor.getString(labelIdx) : null;
                    if (address != null) {
                        contact.getEmails().add(new Contact.ContactField(address, getSafeTypeLabel(type, customLabel), type));
                    }
                }
            }
        } catch (Exception ignored) {}

        // Load Company, Website, Notes
        try (Cursor dataCursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                null,
                ContactsContract.Data.CONTACT_ID + " = ?",
                new String[]{contact.getId()},
                null
        )) {
            if (dataCursor != null) {
                int mimeIdx = dataCursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
                while (dataCursor.moveToNext()) {
                    String mimetype = dataCursor.getString(mimeIdx);
                    if (ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
                        int colIdx = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY);
                        if (colIdx != -1) contact.setCompany(dataCursor.getString(colIdx));
                    } else if (ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE.equals(mimetype)) {
                        int colIdx = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL);
                        if (colIdx != -1) contact.setWebsite(dataCursor.getString(colIdx));
                    } else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(mimetype)) {
                        int colIdx = dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE);
                        if (colIdx != -1) contact.setNotes(dataCursor.getString(colIdx));
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private String getSafeTypeLabel(int type, String customLabel) {
        try {
            if (resources == null) return "Mobile";
            CharSequence label = ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, type, customLabel);
            return label != null ? label.toString() : "Mobile";
        } catch (Exception e) {
            return "Mobile";
        }
    }

    public void updateContact(@NonNull Contact contact, @NonNull UpdateCallback callback) {
        executorService.execute(() -> {
            try {
                if (contact.getId() == null || contact.getId().isEmpty()) {
                    callback.onError("Invalid Contact ID");
                    return;
                }

                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                String contactId = contact.getId();
                String rawContactId = getRawContactId(contactId);

                if (rawContactId == null) {
                    callback.onError("Raw contact not found.");
                    return;
                }

                // 1. Update Name (StructuredName)
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
                                new String[]{rawContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName())
                        .build());

                // 2. Update Starred Status (on the aggregate Contact)
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Contacts.CONTENT_URI)
                        .withSelection(ContactsContract.Contacts._ID + "=?", new String[]{contactId})
                        .withValue(ContactsContract.Contacts.STARRED, contact.isFavorite() ? 1 : 0)
                        .build());

                // 3. Clear existing dynamic fields to re-insert them
                ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " IN (?, ?, ?, ?, ?, ?)",
                                new String[]{rawContactId,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                                        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE})
                        .build());

                // 4. Re-insert Phone Numbers
                for (Contact.ContactField field : contact.getPhoneNumbers()) {
                    String val = field.getValue();
                    if (val == null || val.trim().isEmpty()) continue;
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, val)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, field.getType() != 0 ? field.getType() : ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build());
                }

                // 5. Re-insert Emails
                for (Contact.ContactField field : contact.getEmails()) {
                    String val = field.getValue();
                    if (val == null || val.trim().isEmpty()) continue;
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, val)
                            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, field.getType() != 0 ? field.getType() : ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                            .build());
                }

                // 6. Re-insert Organization
                String company = contact.getCompany();
                if (company != null && !company.trim().isEmpty()) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                            .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                            .build());
                }

                // 7. Re-insert Website
                String website = contact.getWebsite();
                if (website != null && !website.trim().isEmpty()) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Website.URL, website)
                            .withValue(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE)
                            .build());
                }

                // 8. Re-insert Note
                String notes = contact.getNotes();
                if (notes != null && !notes.trim().isEmpty()) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Note.NOTE, notes)
                            .build());
                }

                // 9. Re-insert Photo
                if (contact.getPhotoUri() != null && !contact.getPhotoUri().isEmpty()) {
                    byte[] photoBytes = getPhotoBytes(Uri.parse(contact.getPhotoUri()));
                    if (photoBytes != null) {
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                                .build());
                    }
                }

                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
                refreshContacts();
                callback.onSuccess();
            } catch (SecurityException e) {
                callback.onError("Permission denied to update contact.");
            } catch (IllegalArgumentException e) {
                callback.onError("Invalid data provided for update.");
            } catch (Exception e) {
                Log.e(TAG, "Update failed", e);
                callback.onError(e.getMessage() != null ? e.getMessage() : "Update failed");
            }
        });
    }

    private String getRawContactId(String contactId) {
        String res = null;
        try (Cursor cursor = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{contactId}, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                res = cursor.getString(0);
            }
        }
        return res;
    }

    private byte[] getPhotoBytes(Uri uri) {
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            Log.e(TAG, "Error processing photo", e);
            return null;
        }
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }
}
