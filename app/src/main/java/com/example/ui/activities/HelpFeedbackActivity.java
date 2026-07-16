package com.example.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.R;
import com.example.databinding.ActivityHelpFeedbackBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class HelpFeedbackActivity extends BaseActivity {
    private ActivityHelpFeedbackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupButtons();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupButtons() {
        binding.btnFaq.setOnClickListener(v -> startActivity(new Intent(this, FaqActivity.class)));
        binding.btnSendFeedback.setOnClickListener(v -> sendFeedback());
        binding.btnContactSupport.setOnClickListener(v -> contactSupport());
    }

    private void sendFeedback() {
        String appVersion = "1.0.0";
        try {
            appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception ignored) {}

        String body = String.format(getString(R.string.feedback_body_template),
                Build.MODEL,
                Build.VERSION.RELEASE,
                appVersion);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + getString(R.string.developer_email)));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                showNoEmailDialog();
            }
        } catch (Exception e) {
            showNoEmailDialog();
        }
    }

    private void contactSupport() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + getString(R.string.developer_email)));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body_template));

        try {
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                showNoEmailDialog();
            }
        } catch (Exception e) {
            showNoEmailDialog();
        }
    }

    private void showNoEmailDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.no_email_app_title)
                .setMessage(R.string.no_email_app_msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
