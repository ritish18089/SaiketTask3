package com.example.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.databinding.ActivitySettingsBinding;
import com.example.util.LanguageUtils;
import com.example.util.ThemeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends BaseActivity {
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.llBlockedNumbers.setOnClickListener(v -> startActivity(new Intent(this, BlockedNumbersActivity.class)));
        binding.llTheme.setOnClickListener(v -> showThemeDialog());
        addLanguageOption();
        binding.llSpamDetection.setOnClickListener(v -> startActivity(new Intent(this, SpamDetectionActivity.class)));
        binding.llAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
        binding.llHelpFeedback.setOnClickListener(v -> startActivity(new Intent(this, HelpFeedbackActivity.class)));
    }

    private void addLanguageOption() {
        LinearLayout container = (LinearLayout) binding.llTheme.getParent();
        int index = container.indexOfChild(binding.llTheme) + 1;

        View languageView = getLayoutInflater().inflate(com.example.R.layout.item_setting_option, container, false);
        TextView title = languageView.findViewById(com.example.R.id.tvTitle);
        TextView subtitle = languageView.findViewById(com.example.R.id.tvSubtitle);
        
        title.setText(getString(com.example.R.string.app_language));
        subtitle.setText(getString(com.example.R.string.select_language_subtitle));
        
        languageView.setOnClickListener(v -> showLanguageDialog());
        container.addView(languageView, index);
    }

    private void showThemeDialog() {
        String[] themes = {getString(com.example.R.string.theme_light), getString(com.example.R.string.theme_dark)};
        int checkedItem = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(com.example.R.string.choose_theme))
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int mode = (which == 1) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
                    ThemeUtils.setSelectedTheme(this, mode);
                    dialog.dismiss();
                })
                .setNegativeButton(getString(com.example.R.string.cancel), null)
                .show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "हिन्दी (Hindi)", "ಕನ್ನಡ (Kannada)", "తెలుగు (Telugu)", "தமிழ் (Tamil)", "മലയാളം (Malayalam)", "বাংলা (Bengali)", "ગુજરાતી (Gujarati)", "मराठी (Marathi)", "ਪੰਜਾਬੀ (Punjabi)", "ଓಡ଼ಿଆ (Odia)", "অসমীয়া (Assamese)"};
        String[] langCodes = {"en", "hi", "kn", "te", "ta", "ml", "bn", "gu", "mr", "pa", "or", "as"};
        
        String currentLang = LanguageUtils.getSelectedLanguage(this);
        int checkedItem = 0;
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(com.example.R.string.choose_app_language))
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    LanguageUtils.setSelectedLanguage(this, langCodes[which]);
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton(getString(com.example.R.string.cancel), null)
                .show();
    }
}
