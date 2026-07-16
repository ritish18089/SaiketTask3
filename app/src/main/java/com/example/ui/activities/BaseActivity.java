package com.example.ui.activities;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.util.LanguageUtils;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtils.wrap(newBase));
    }
}
