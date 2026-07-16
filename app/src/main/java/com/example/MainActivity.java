package com.example;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.databinding.ActivityMainBinding;
import com.example.ui.activities.BaseActivity;
import com.example.ui.fragments.ContactsFragment;
import com.example.ui.fragments.FavoritesFragment;
import com.example.ui.fragments.HistoryFragment;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;

    private final ActivityResultLauncher<Intent> defaultDialerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                checkDefaultDialer();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.util.ThemeUtils.applyTheme(this);
        com.example.util.LanguageUtils.applyLanguage(this);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, 0);
            binding.bottomNavigation.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        setupBottomNavigation();
        checkDefaultDialer();

        binding.fabDial.setOnClickListener(v -> startActivity(new Intent(this, com.example.ui.activities.DialpadActivity.class)));

        if (savedInstanceState == null) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_contacts);
            switchFragment(new ContactsFragment());
        }
    }


    private void checkDefaultDialer() {
        boolean isDefault = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                isDefault = roleManager.isRoleHeld(RoleManager.ROLE_DIALER);
            }
        } else {
            TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                String defaultDialer = telecomManager.getDefaultDialerPackage();
                isDefault = getPackageName().equals(defaultDialer);
            }
        }
    }

    private void requestDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER);
                defaultDialerLauncher.launch(intent);
            }
        } else {
            Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
            defaultDialerLauncher.launch(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDefaultDialer();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favorites) {
                switchFragment(new FavoritesFragment());
                return true;
            } else if (id == R.id.nav_history) {
                switchFragment(new HistoryFragment());
                return true;
            } else if (id == R.id.nav_contacts) {
                switchFragment(new ContactsFragment());
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(binding.container.getId(), fragment)
                .commit();
    }
}
