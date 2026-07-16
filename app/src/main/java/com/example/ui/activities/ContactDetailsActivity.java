package com.example.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.databinding.ActivityContactDetailsBinding;
import com.example.model.Contact;
import com.example.viewmodel.ContactViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashSet;
import java.util.Set;

public class ContactDetailsActivity extends BaseActivity {
    private ActivityContactDetailsBinding binding;
    private Contact contact;
    private ContactViewModel viewModel;

    private static final int PERMISSIONS_REQUEST_CALL = 101;

    private final ActivityResultLauncher<Intent> editContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshContact();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contact = (Contact) getIntent().getSerializableExtra("contact");

        setupToolbar();
        displayContactDetails();
        setupActions();
        observeViewModel();
    }

    private void refreshContact() {
        if (contact == null || contact.getId() == null) return;
        final String contactId = contact.getId();
        viewModel.getAllContacts().observe(this, contacts -> {
            for (Contact c : contacts) {
                if (contactId.equals(c.getId())) {
                    contact = c;
                    displayContactDetails();
                    break;
                }
            }
            viewModel.getAllContacts().removeObservers(this);
        });
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.toolbar.inflateMenu(com.example.R.menu.menu_contact_details);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == com.example.R.id.action_edit) {
                Intent intent = new Intent(this, EditContactActivity.class);
                intent.putExtra("contact", contact);
                editContactLauncher.launch(intent);
                return true;
            } else if (id == com.example.R.id.action_favorite) {
                toggleFavorite();
                return true;
            } else if (id == com.example.R.id.action_share) {
                shareContact();
                return true;
            } else if (id == com.example.R.id.action_delete) {
                confirmDelete();
                return true;
            } else if (id == com.example.R.id.action_view_history) {
                viewCallHistory();
                return true;
            } else if (id == com.example.R.id.action_clear_history) {
                confirmClearHistory();
                return true;
            } else if (id == com.example.R.id.action_block) {
                blockContact();
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        viewModel.getUpdateStatus().observe(this, status -> {
            if ("DELETED".equals(status)) {
                Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
                finish();
            } else if ("HISTORY_CLEARED".equals(status)) {
                Toast.makeText(this, "Call history cleared", Toast.LENGTH_SHORT).show();
            } else if ("SUCCESS".equals(status)) {
                refreshContact();
            } else if (status != null && !status.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Update Failed")
                        .setMessage("Unable to update the contact. Please try again.\nError: " + status)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void displayContactDetails() {
        if (contact != null) {
            binding.tvDetailName.setText(contact.getName());
            
            if (contact.getPhotoUri() != null) {
                binding.ivBigAvatar.setImageURI(Uri.parse(contact.getPhotoUri()));
            } else {
                binding.ivBigAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            updateFavoriteIcon();
            
            binding.llPhoneNumbersContainer.removeAllViews();
            
            // Show only unique phone numbers
            Set<String> seenNumbers = new HashSet<>();
            for (Contact.ContactField field : contact.getPhoneNumbers()) {
                String val = field.getValue();
                if (val == null) continue;
                String normalized = val.replaceAll("\\s+", "");
                if (!seenNumbers.contains(normalized)) {
                    addPhoneCard(field);
                    seenNumbers.add(normalized);
                }
            }
        }
    }

    private void addPhoneCard(Contact.ContactField field) {
        View cardView = LayoutInflater.from(this).inflate(com.example.R.layout.item_contact_info_card, binding.llPhoneNumbersContainer, false);
        
        TextView tvNumber = cardView.findViewById(com.example.R.id.tvPhoneNumber);
        TextView tvLabel = cardView.findViewById(com.example.R.id.tvPhoneType);
        ImageButton btnMessage = cardView.findViewById(com.example.R.id.btnMessageIcon);
        ImageButton btnCall = cardView.findViewById(com.example.R.id.btnCallIcon);

        tvNumber.setText(field.getValue());
        tvLabel.setText(field.getLabel());

        btnMessage.setOnClickListener(v -> sendMessage(field.getValue()));
        btnCall.setOnClickListener(v -> makeCall(field.getValue()));

        binding.llPhoneNumbersContainer.addView(cardView);
    }

    private void setupActions() {
        binding.btnCall.setOnClickListener(v -> {
            if (!contact.getPhoneNumbers().isEmpty()) {
                makeCall(contact.getPhoneNumbers().get(0).getValue());
            }
        });
        
        binding.btnText.setOnClickListener(v -> {
            if (!contact.getPhoneNumbers().isEmpty()) {
                sendMessage(contact.getPhoneNumbers().get(0).getValue());
            }
        });

        binding.btnVideo.setOnClickListener(v -> startVideoCall());
    }

    private void makeCall(String number) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSIONS_REQUEST_CALL);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }

    private void sendMessage(String number) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
        startActivity(intent);
    }

    private void startVideoCall() {
        if (contact == null || contact.getPhoneNumbers().isEmpty()) {
            Toast.makeText(this, "Invalid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String number = contact.getPhoneNumbers().get(0).getValue();
        if (number == null || number.trim().isEmpty()) {
            Toast.makeText(this, "Invalid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Standard Android Intent mechanism for video calling
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("tel:" + number));
            intent.putExtra("android.telecom.extra.START_WITH_VIDEO_CALL", true);
            intent.putExtra("videocall", 1); // Older fallback extra

            if (intent.resolveActivity(getPackageManager()) != null) {
                // Display the system app chooser if multiple apps are available
                startActivity(Intent.createChooser(intent, "Video Call"));
            } else {
                Toast.makeText(this, "No video calling application available.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "No video calling application available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite() {
        contact.setFavorite(!contact.isFavorite());
        viewModel.updateContact(contact);
        updateFavoriteIcon();
        Toast.makeText(this, contact.isFavorite() ? "Added to Favorites" : "Removed from Favorites", Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteIcon() {
        android.view.MenuItem favoriteItem = binding.toolbar.getMenu().findItem(com.example.R.id.action_favorite);
        if (favoriteItem != null) {
            favoriteItem.setIcon(contact.isFavorite() ? 
                android.R.drawable.btn_star_big_on : 
                android.R.drawable.btn_star_big_off);
        }
    }

    private void shareContact() {
        String vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:" + contact.getName() + "\n";
        for (Contact.ContactField field : contact.getPhoneNumbers()) {
            vcard += "TEL;TYPE=CELL:" + field.getValue() + "\n";
        }
        vcard += "END:VCARD";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/x-vcard");
        intent.putExtra(Intent.EXTRA_TEXT, vcard);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contact: " + contact.getName());
        startActivity(Intent.createChooser(intent, "Share Contact"));
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteContact(contact.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void viewCallHistory() {
        if (contact.getPhoneNumbers().isEmpty()) {
            Toast.makeText(this, "No phone numbers for this contact", Toast.LENGTH_SHORT).show();
            return;
        }
        java.util.ArrayList<String> numbers = new java.util.ArrayList<>();
        for (Contact.ContactField field : contact.getPhoneNumbers()) {
            numbers.add(field.getValue());
        }
        Intent intent = new Intent(this, ContactCallHistoryActivity.class);
        intent.putStringArrayListExtra("phone_numbers", numbers);
        intent.putExtra("contact_name", contact.getName());
        startActivity(intent);
    }

    private void confirmClearHistory() {
        if (contact.getPhoneNumbers().isEmpty()) {
            Toast.makeText(this, "No phone numbers for this contact", Toast.LENGTH_SHORT).show();
            return;
        }
        java.util.ArrayList<String> numbers = new java.util.ArrayList<>();
        for (Contact.ContactField field : contact.getPhoneNumbers()) {
            numbers.add(field.getValue());
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear Call History")
                .setMessage("Are you sure you want to clear the call history for this contact?")
                .setPositiveButton("Clear", (dialog, which) -> viewModel.clearCallHistoryForContact(numbers))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void blockContact() {
        if (contact == null || contact.getPhoneNumbers().isEmpty()) return;
        String number = contact.getPhoneNumbers().get(0).getValue();
        if (number == null) return;
        final String normalizedNumber = number.replaceAll("\\s+", "");

        viewModel.getBlockedNumbers().observe(this, blockedNumbers -> {
            boolean alreadyBlocked = false;
            for (com.example.data.models.BlockedNumber bn : blockedNumbers) {
                String bnNum = bn.getNumber();
                if (bnNum != null && bnNum.replaceAll("\\s+", "").equals(normalizedNumber)) {
                    alreadyBlocked = true;
                    break;
                }
            }

            if (alreadyBlocked) {
                Toast.makeText(this, "This contact is already blocked.", Toast.LENGTH_SHORT).show();
            } else {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Block Contact")
                        .setMessage("Block this contact from calling you?")
                        .setPositiveButton("Block", (dialog, which) -> {
                            viewModel.blockNumber(number, contact.getName());
                            Toast.makeText(this, "Contact blocked successfully", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            viewModel.getBlockedNumbers().removeObservers(this);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!contact.getPhoneNumbers().isEmpty()) {
                    makeCall(contact.getPhoneNumbers().get(0).getValue());
                }
            } else {
                Toast.makeText(this, "Permission denied to make calls", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
