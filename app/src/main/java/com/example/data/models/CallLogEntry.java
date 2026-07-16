package com.example.data.models;

public class CallLogEntry {
    private String number;
    private String name;
    private int type;
    private long date;
    private String duration;
    private String photoUri;

    public CallLogEntry(String number, String name, int type, long date, String duration, String photoUri) {
        this.number = number;
        this.name = name;
        this.type = type;
        this.date = date;
        this.duration = duration;
        this.photoUri = photoUri;
    }

    public String getNumber() { return number; }
    public String getName() { return name; }
    public int getType() { return type; }
    public long getDate() { return date; }
    public String getDuration() { return duration; }
    public String getPhotoUri() { return photoUri; }
}
