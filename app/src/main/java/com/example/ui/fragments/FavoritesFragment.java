package com.example.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.adapter.ContactAdapter;
import com.example.databinding.FragmentFavoritesBinding;
import com.example.model.Contact;
import com.example.ui.activities.ContactDetailsActivity;
import com.example.viewmodel.ContactViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {
    private FragmentFavoritesBinding binding;
    private ContactViewModel viewModel;
    private ContactAdapter adapter;
    private List<Contact> allFavorites = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ContactViewModel.class);
        setupRecyclerView();
        setupToolbar();
        setupSearch();
        loadFavorites();
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
                // Call logic
            }
        });
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFavorites(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFavorites(String query) {
        final String lowerQuery = query == null ? "" : query.toLowerCase().trim();
        if (lowerQuery.isEmpty()) {
            adapter.setContacts(allFavorites);
            return;
        }

        List<Contact> filtered = new ArrayList<>();
        for (Contact contact : allFavorites) {
            String name = contact.getName() == null ? "" : contact.getName().toLowerCase();
            if (name.contains(lowerQuery)) {
                filtered.add(contact);
                continue;
            }
            for (Contact.ContactField field : contact.getPhoneNumbers()) {
                String value = field.getValue() == null ? "" : field.getValue();
                if (value.contains(lowerQuery)) {
                    filtered.add(contact);
                    break;
                }
            }
        }
        adapter.setContacts(filtered);
    }

    private void loadFavorites() {
        viewModel.getFavoriteContacts().observe(getViewLifecycleOwner(), favorites -> {
            allFavorites = favorites;
            adapter.setContacts(favorites);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
