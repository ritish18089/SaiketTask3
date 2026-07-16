package com.example.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.data.models.BlockedNumber;
import java.util.List;

@Dao
public interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers")
    LiveData<List<BlockedNumber>> getAllBlockedNumbers();

    @Insert
    void insert(BlockedNumber blockedNumber);

    @Delete
    void delete(BlockedNumber blockedNumber);

    @Query("SELECT EXISTS(SELECT * FROM blocked_numbers WHERE number = :number)")
    boolean isBlocked(String number);
}
