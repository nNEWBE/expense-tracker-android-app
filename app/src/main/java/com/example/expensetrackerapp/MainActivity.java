package com.example.expensetrackerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.expensetrackerapp.auth.AuthManager;
import com.example.expensetrackerapp.auth.LoginActivity;
import com.example.expensetrackerapp.databinding.ActivityMainBinding;
import com.example.expensetrackerapp.ui.analytics.AnalyticsFragment;
import com.example.expensetrackerapp.ui.dashboard.DashboardFragment;
import com.example.expensetrackerapp.ui.profile.ProfileFragment;
import com.example.expensetrackerapp.ui.transactions.AddExpenseBottomSheet;
import com.example.expensetrackerapp.ui.transactions.TransactionsFragment;
import com.example.expensetrackerapp.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private ActivityMainBinding binding;
    private AuthManager authManager;
    private PreferenceManager preferenceManager;

    // Fragment tags
    private static final String TAG_DASHBOARD = "dashboard";
    private static final String TAG_TRANSACTIONS = "transactions";
    private static final String TAG_ANALYTICS = "analytics";
    private static final String TAG_PROFILE = "profile";

    private String currentFragmentTag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance();
        preferenceManager = PreferenceManager.getInstance(this);

        // Apply saved theme
        preferenceManager.applyTheme(preferenceManager.getThemeMode());

        setupBottomNavigation();
        setupGuestBanner();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment(), TAG_DASHBOARD);
        }
    }

    private void setupBottomNavigation() {
        // Dashboard
        binding.navDashboard.setOnClickListener(v -> {
            selectNavItem(TAG_DASHBOARD);
            loadFragment(new DashboardFragment(), TAG_DASHBOARD);
        });

        // Transactions
        binding.navTransactions.setOnClickListener(v -> {
            selectNavItem(TAG_TRANSACTIONS);
            loadFragment(new TransactionsFragment(), TAG_TRANSACTIONS);
        });

        // Analytics
        binding.navAnalytics.setOnClickListener(v -> {
            selectNavItem(TAG_ANALYTICS);
            loadFragment(new AnalyticsFragment(), TAG_ANALYTICS);
        });

        // Profile
        binding.navProfile.setOnClickListener(v -> {
            selectNavItem(TAG_PROFILE);
            loadFragment(new ProfileFragment(), TAG_PROFILE);
        });

        // FAB Add Button
        binding.fabAdd.setOnClickListener(v -> {
            AddExpenseBottomSheet bottomSheet = new AddExpenseBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddExpenseBottomSheet");
        });

        // Set initial selection
        selectNavItem(TAG_DASHBOARD);
    }

    private void selectNavItem(String tag) {
        int activeColor = getResources().getColor(R.color.primary, getTheme());
        int inactiveColor = getResources().getColor(R.color.text_hint, getTheme());

        binding.ivDashboard.setColorFilter(tag.equals(TAG_DASHBOARD) ? activeColor : inactiveColor);
        binding.ivTransactions.setColorFilter(tag.equals(TAG_TRANSACTIONS) ? activeColor : inactiveColor);
        binding.ivAnalytics.setColorFilter(tag.equals(TAG_ANALYTICS) ? activeColor : inactiveColor);
        binding.ivProfile.setColorFilter(tag.equals(TAG_PROFILE) ? activeColor : inactiveColor);
    }

    private void setupGuestBanner() {
        if (authManager.isGuest()) {
            binding.guestBanner.setVisibility(View.VISIBLE);
            binding.btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        } else {
            binding.guestBanner.setVisibility(View.GONE);
        }
    }

    private void loadFragment(Fragment fragment, String tag) {
        if (tag.equals(currentFragmentTag)) {
            return;
        }

        currentFragmentTag = tag;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left);
        transaction.replace(R.id.fragmentContainer, fragment, tag);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        authManager.setAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        authManager.removeAuthStateListener();
    }

    @Override
    public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
        setupGuestBanner();
    }

    public void refreshDashboard() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_DASHBOARD);
        if (fragment instanceof DashboardFragment) {
            ((DashboardFragment) fragment).refreshData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}