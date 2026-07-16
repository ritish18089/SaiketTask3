package com.example.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.CallHistoryAdapter;
import com.example.data.models.CallLogEntry;
import com.example.databinding.FragmentHistoryBinding;
import com.example.viewmodel.ContactViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private ContactViewModel viewModel;
    private CallHistoryAdapter adapter;
    private List<CallLogEntry> allHistory = new ArrayList<>();
    private String pendingPhoneNumber;
    private static final int PERMISSION_CALL_PHONE = 201;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        setupRecyclerView();
        setupToolbar();
        setupSearch();
        checkPermissions();
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(com.example.R.menu.menu_main);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == com.example.R.id.action_settings) {
                startActivity(new Intent(getActivity(), com.example.ui.activities.SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new CallHistoryAdapter();
        adapter.setOnCallClickListener(entry -> initiateCall(entry.getNumber()));
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHistory.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHistory(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterHistory(String query) {
        final String lowerQuery = query == null ? "" : query.toLowerCase().trim();
        if (lowerQuery.isEmpty()) {
            adapter.setHistory(allHistory);
            return;
        }

        List<CallLogEntry> filtered = new ArrayList<>();
        for (CallLogEntry entry : allHistory) {
            String name = entry.getName() == null ? "" : entry.getName().toLowerCase();
            String number = entry.getNumber() == null ? "" : entry.getNumber();
            if (name.contains(lowerQuery)) {
                filtered.add(entry);
            } else if (number.contains(lowerQuery)) {
                filtered.add(entry);
            }
        }
        adapter.setHistory(filtered);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            loadHistory();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, 200);
        }
    }

    private void loadHistory() {
        viewModel.getCallHistory().observe(getViewLifecycleOwner(), history -> {
            allHistory = history;
            adapter.setHistory(history);
        });
    }

    private void initiateCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Phone number not available.", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        pendingPhoneNumber = phoneNumber;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActualCall();
        } else {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_PHONE);
        }
    }

    private void startActualCall() {
        if (pendingPhoneNumber == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(android.net.Uri.parse("tel:" + pendingPhoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(), "Error making call: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        } finally {
            pendingPhoneNumber = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadHistory();
        } else if (requestCode == PERMISSION_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActualCall();
            } else {
                android.widget.Toast.makeText(requireContext(), "Call permission denied", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
