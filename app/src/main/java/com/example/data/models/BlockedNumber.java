package com.example.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "blocked_numbers")
public class BlockedNumber {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String number;
    private String label;

    public BlockedNumber(String number, String label) {
        this.number = number;
        this.label = label;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
