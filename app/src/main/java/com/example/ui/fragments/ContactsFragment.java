package com.example.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.ContactAdapter;
import com.example.databinding.FragmentContactsBinding;
import com.example.model.Contact;
import com.example.ui.activities.ContactDetailsActivity;
import com.example.viewmodel.ContactViewModel;

public class ContactsFragment extends Fragment {
    private FragmentContactsBinding binding;
    private ContactViewModel viewModel;
    private ContactAdapter adapter;

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        setupToolbar();
        setupUI();
        setupRecyclerView();
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

    private void setupUI() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        binding.ivMic.setOnClickListener(v -> startVoiceSearch());
    }

    private void filterContacts(String query) {
        final String safeQuery = query == null ? "" : query.toLowerCase().trim();
        viewModel.getAllContacts().observe(getViewLifecycleOwner(), contacts -> {
            java.util.List<com.example.model.Contact> filtered = new java.util.ArrayList<>();
            for (com.example.model.Contact c : contacts) {
                String name = c.getName() == null ? "" : c.getName().toLowerCase();
                if (name.contains(safeQuery)) {
                    filtered.add(c);
                }
            }
            adapter.setContacts(filtered);
        });
    }

    private final androidx.activity.result.ActivityResultLauncher<Intent> voiceSearchLauncher = 
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                java.util.ArrayList<String> matches = result.getData().getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                    binding.etSearch.setText(matches.get(0));
                }
            }
        });

    private void startVoiceSearch() {
        com.example.util.VoiceSearchHelper.startVoiceSearch(voiceSearchLauncher);
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter(new ContactAdapter.OnContactClickListener() {
            @Override
            public void onContactClick(Contact contact, View sharedElement) {
                Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
                intent.putExtra("contact", contact);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(), sharedElement, "avatar_transition");
                startActivity(intent, options.toBundle());
            }

            @Override
            public void onCallClick(Contact contact) {
                if (contact.getPhoneNumbers().isEmpty()) return;
                String number = contact.getPhoneNumbers().get(0).getValue();
                Intent intent = new Intent(Intent.ACTION_CALL, android.net.Uri.parse("tel:" + number));
                if (androidx.core.content.ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CALL_PHONE)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Call permission required", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.rvContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvContacts.setAdapter(adapter);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    private void loadContacts() {
        viewModel.getAllContacts().observe(getViewLifecycleOwner(), contacts -> {
            adapter.setContacts(contacts);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(getContext(), "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
