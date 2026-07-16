package com.example.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.data.models.BlockedNumber;

@Database(entities = {BlockedNumber.class}, version = 1, exportSchema = false)
public abstract class AppDatabaseJava extends RoomDatabase {
    public abstract BlockedNumberDao blockedNumberDao();

    private static volatile AppDatabaseJava INSTANCE;

    public static AppDatabaseJava getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabaseJava.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabaseJava.class, "smart_contacts_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
