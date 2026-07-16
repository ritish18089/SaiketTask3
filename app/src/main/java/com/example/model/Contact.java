package com.example.model;

import android.net.Uri;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Contact implements Serializable {
    private String id;
    private String lookupKey;
    private String name;
    private String photoUri;
    private List<ContactField> phoneNumbers = new ArrayList<>();
    private List<ContactField> emails = new ArrayList<>();
    private String company;
    private String jobTitle;
    private String website;
    private String address;
    private String birthday;
    private String notes;
    private String nickname;
    private boolean isFavorite;

    public Contact() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLookupKey() { return lookupKey; }
    public void setLookupKey(String lookupKey) { this.lookupKey = lookupKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }

    public List<ContactField> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(List<ContactField> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

    public List<ContactField> getEmails() { return emails; }
    public void setEmails(List<ContactField> emails) { this.emails = emails; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public static class ContactField implements Serializable {
        private String value;
        private String label;
        private int type;

        public ContactField(String value, String label, int type) {
            this.value = value;
            this.label = label;
            this.type = type;
        }

        public String getValue() { return value; }
        public String getLabel() { return label; }
        public int getType() { return type; }
    }
}
