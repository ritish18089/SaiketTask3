package com.example.ui.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.databinding.ActivityDialpadBinding;

public class DialpadActivity extends BaseActivity {
    private ActivityDialpadBinding binding;
    private StringBuilder numberBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDialpadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupKeypad();
        binding.btnCall.setOnClickListener(v -> makeCall());
        binding.btnDelete.setOnClickListener(v -> deleteDigit());
        
        // Fix: Implement Add Contact and Video Call buttons
        binding.btnAddContact.setOnClickListener(v -> addContact());
        binding.btnVideoCall.setOnClickListener(v -> startVideoCall());
    }

    private void addContact() {
        String number = numberBuilder.toString();
        if (number.isEmpty()) {
            Toast.makeText(this, "Enter a phone number first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Unable to open contacts app", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVideoCall() {
        String number = numberBuilder.toString();
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


    private void setupKeypad() {
        binding.btn1.setOnClickListener(v -> appendDigit("1"));
        binding.btn2.setOnClickListener(v -> appendDigit("2"));
        binding.btn3.setOnClickListener(v -> appendDigit("3"));
        binding.btn4.setOnClickListener(v -> appendDigit("4"));
        binding.btn5.setOnClickListener(v -> appendDigit("5"));
        binding.btn6.setOnClickListener(v -> appendDigit("6"));
        binding.btn7.setOnClickListener(v -> appendDigit("7"));
        binding.btn8.setOnClickListener(v -> appendDigit("8"));
        binding.btn9.setOnClickListener(v -> appendDigit("9"));
        binding.btn0.setOnClickListener(v -> appendDigit("0"));
        binding.btnStar.setOnClickListener(v -> appendDigit("*"));
        binding.btnHash.setOnClickListener(v -> appendDigit("#"));
    }

    private void appendDigit(String digit) {
        numberBuilder.append(digit);
        binding.tvNumber.setText(numberBuilder.toString());
    }

    private void deleteDigit() {
        if (numberBuilder.length() > 0) {
            numberBuilder.deleteCharAt(numberBuilder.length() - 1);
            binding.tvNumber.setText(numberBuilder.toString());
        }
    }

    private void makeCall() {
        String number = numberBuilder.toString();
        if (!number.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }
}
