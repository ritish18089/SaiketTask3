package com.example.util;

import android.content.Intent;
import android.speech.RecognizerIntent;
import androidx.activity.result.ActivityResultLauncher;
import java.util.Locale;

public class VoiceSearchHelper {
    public static void startVoiceSearch(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak contact name...");
        launcher.launch(intent);
    }
}
