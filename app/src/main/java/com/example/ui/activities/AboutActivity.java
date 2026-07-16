package com.example.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.databinding.ActivityAboutBinding;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.btnPrivacyPolicy.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }
}
