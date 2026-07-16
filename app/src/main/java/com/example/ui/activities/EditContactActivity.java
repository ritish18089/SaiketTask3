package com.example.ui.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.example.databinding.ActivityEditContactBinding;
import com.example.model.Contact;
import com.example.viewmodel.ContactViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditContactActivity extends BaseActivity {
    private ActivityEditContactBinding binding;
    private ContactViewModel viewModel;
    private Contact contact;
    private Uri photoUri;

    private final ActivityResultLauncher<String> requestWritePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    performUpdate();
                } else {
                    handlePermissionDenied();
                }
            });

    private final ActivityResultLauncher<String> requestPhotoPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showPhotoOptions();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    binding.ivEditAvatar.setImageURI(photoUri);
                    if (contact != null) contact.setPhotoUri(photoUri.toString());
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoUri = uri;
                    binding.ivEditAvatar.setImageURI(photoUri);
                    if (contact != null) contact.setPhotoUri(photoUri.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contact = (Contact) getIntent().getSerializableExtra("contact");

        setupToolbar();
        populateFields();
        setupListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.btnUpdate.setOnClickListener(v -> checkPermissionAndUpdate());
    }

    private void populateFields() {
        if (contact != null) {
            binding.etName.setText(contact.getName());
            binding.etCompany.setText(contact.getCompany());
            binding.etWebsite.setText(contact.getWebsite());
            binding.etNotes.setText(contact.getNotes());
            
            if (contact.getPhotoUri() != null) {
                try {
                    binding.ivEditAvatar.setImageURI(Uri.parse(contact.getPhotoUri()));
                } catch (Exception e) {
                    binding.ivEditAvatar.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            }

            // Populate phone numbers
            binding.llPhoneFields.removeAllViews();
            if (contact.getPhoneNumbers().isEmpty()) {
                addDynamicField(binding.llPhoneFields, "Phone", InputType.TYPE_CLASS_PHONE, "");
            } else {
                for (Contact.ContactField field : contact.getPhoneNumbers()) {
                    addDynamicField(binding.llPhoneFields, "Phone", InputType.TYPE_CLASS_PHONE, field.getValue());
                }
            }

            // Populate emails
            binding.llEmailFields.removeAllViews();
            if (contact.getEmails().isEmpty()) {
                addDynamicField(binding.llEmailFields, "Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, "");
            } else {
                for (Contact.ContactField field : contact.getEmails()) {
                    addDynamicField(binding.llEmailFields, "Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, field.getValue());
                }
            }
        }
    }

    private void addDynamicField(LinearLayout container, String hint, int inputType, String value) {
        View fieldView = LayoutInflater.from(this).inflate(com.example.R.layout.item_dynamic_field, container, false);
        TextInputLayout inputLayout = fieldView.findViewById(com.example.R.id.textInputLayout);
        TextInputEditText editText = fieldView.findViewById(com.example.R.id.editText);
        View btnRemove = fieldView.findViewById(com.example.R.id.btnRemove);

        inputLayout.setHint(hint);
        editText.setInputType(inputType);
        editText.setText(value);

        btnRemove.setOnClickListener(v -> {
            if (container.getChildCount() > 1) {
                container.removeView(fieldView);
            } else {
                editText.setText("");
            }
        });

        container.addView(fieldView);
    }

    private void setupListeners() {
        binding.btnAddPhone.setOnClickListener(v -> addDynamicField(binding.llPhoneFields, "Phone", InputType.TYPE_CLASS_PHONE, ""));
        binding.btnAddEmail.setOnClickListener(v -> addDynamicField(binding.llEmailFields, "Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, ""));
        binding.fabEditPhoto.setOnClickListener(v -> showPhotoOptions());
    }

    private void showPhotoOptions() {
        String[] options = {"Take Photo", "Select from Gallery", "Cancel"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Update Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndLaunch();
                    } else if (which == 1) {
                        checkStoragePermissionAndLaunch();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPhotoPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkStoragePermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*");
            } else {
                requestPhotoPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*");
            } else {
                requestPhotoPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void launchCamera() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePhotoLauncher.launch(photoUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void checkPermissionAndUpdate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            performUpdate();
        } else {
            requestWritePermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS);
        }
    }

    private void handlePermissionDenied() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {
            // Permanently denied
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Permission Required")
                    .setMessage("Contacts permission is required to update contacts. Please enable it in app settings.")
                    .setPositiveButton("Settings", (dialog, which) -> openAppSettings())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Permission Required")
                    .setMessage("Contacts permission is required to update contacts.")
                    .setPositiveButton("Grant Permission", (dialog, which) -> requestWritePermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Could not open settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void performUpdate() {
        if (contact == null) return;

        try {
            String name = binding.etName.getText() != null ? binding.etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                binding.etName.setError("Name is required");
                return;
            }
            contact.setName(name);
            
            String company = binding.etCompany.getText() != null ? binding.etCompany.getText().toString().trim() : "";
            contact.setCompany(company);
            
            String website = binding.etWebsite.getText() != null ? binding.etWebsite.getText().toString().trim() : "";
            contact.setWebsite(website);
            
            String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";
            contact.setNotes(notes);

            // Update phone numbers and handle duplicates
            List<Contact.ContactField> phoneNumbers = new ArrayList<>();
            java.util.Set<String> seenNumbers = new java.util.HashSet<>();
            for (int i = 0; i < binding.llPhoneFields.getChildCount(); i++) {
                View v = binding.llPhoneFields.getChildAt(i);
                EditText et = v.findViewById(com.example.R.id.editText);
                if (et != null && et.getText() != null) {
                    String value = et.getText().toString().trim();
                    if (!value.isEmpty()) {
                        String normalized = value.replaceAll("[^0-9+]", "");
                        if (!seenNumbers.contains(normalized)) {
                            phoneNumbers.add(new Contact.ContactField(value, "Mobile", android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE));
                            seenNumbers.add(normalized);
                        }
                    }
                }
            }
            contact.setPhoneNumbers(phoneNumbers);

            // Update emails
            List<Contact.ContactField> emails = new ArrayList<>();
            for (int i = 0; i < binding.llEmailFields.getChildCount(); i++) {
                View v = binding.llEmailFields.getChildAt(i);
                EditText et = v.findViewById(com.example.R.id.editText);
                if (et != null && et.getText() != null) {
                    String value = et.getText().toString().trim();
                    if (!value.isEmpty()) {
                        emails.add(new Contact.ContactField(value, "Home", android.provider.ContactsContract.CommonDataKinds.Email.TYPE_HOME));
                    }
                }
            }
            contact.setEmails(emails);

            viewModel.updateContact(contact);
        } catch (Exception e) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Update Failed")
                    .setMessage("Unable to update the contact. Please try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void observeViewModel() {
        viewModel.getUpdateStatus().observe(this, status -> {
            if ("SUCCESS".equals(status)) {
                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else if (status != null && !status.isEmpty()) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Update Failed")
                        .setMessage("Unable to update the contact. Please try again.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }
}
