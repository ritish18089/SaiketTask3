package com.example.ui.activities;

import android.os.Bundle;
import com.example.R;
import com.example.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPrivacyPolicyBinding binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
