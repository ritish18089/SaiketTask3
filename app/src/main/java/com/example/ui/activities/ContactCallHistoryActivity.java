package com.example.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.adapter.CallHistoryAdapter;
import com.example.databinding.ActivityContactCallHistoryBinding;
import com.example.viewmodel.ContactViewModel;

public class ContactCallHistoryActivity extends BaseActivity {
    private ActivityContactCallHistoryBinding binding;
    private ContactViewModel viewModel;
    private CallHistoryAdapter adapter;
    private java.util.List<String> phoneNumbers;
    private String pendingPhoneNumber;
    private static final int PERMISSION_CALL_PHONE = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactCallHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        phoneNumbers = getIntent().getStringArrayListExtra("phone_numbers");
        String name = getIntent().getStringExtra("contact_name");
        
        if (name != null) {
            binding.toolbar.setTitle(name);
        }

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        setupRecyclerView();
        
        if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
            viewModel.getCallHistoryForContact(phoneNumbers).observe(this, history -> {
                adapter.setHistory(history);
                if (history == null || history.isEmpty()) {
                    binding.rvHistory.setVisibility(android.view.View.GONE);
                    binding.llEmptyState.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.rvHistory.setVisibility(android.view.View.VISIBLE);
                    binding.llEmptyState.setVisibility(android.view.View.GONE);
                }
            });
        } else {
            binding.rvHistory.setVisibility(android.view.View.GONE);
            binding.llEmptyState.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new CallHistoryAdapter();
        adapter.setOnCallClickListener(entry -> initiateCall(entry.getNumber()));
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void initiateCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            android.widget.Toast.makeText(this, "Phone number not available.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        pendingPhoneNumber = phoneNumber;
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            startActualCall();
        } else {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, PERMISSION_CALL_PHONE);
        }
    }

    private void startActualCall() {
        if (pendingPhoneNumber == null) return;
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_CALL);
            intent.setData(android.net.Uri.parse("tel:" + pendingPhoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Error making call: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        } finally {
            pendingPhoneNumber = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startActualCall();
            } else {
                android.widget.Toast.makeText(this, "Call permission denied", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
}
