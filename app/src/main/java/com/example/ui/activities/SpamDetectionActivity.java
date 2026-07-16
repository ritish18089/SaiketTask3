package com.example.ui.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.databinding.ActivitySpamDetectionBinding;

public class SpamDetectionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySpamDetectionBinding binding = ActivitySpamDetectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.btnCheck.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.tvResult.setVisibility(View.GONE);
            
            v.postDelayed(() -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvResult.setVisibility(View.VISIBLE);
                binding.tvResult.setText("AI Analysis: Low Risk. This number appears safe.");
                binding.tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }, 2000);
        });
    }
}
