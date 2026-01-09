package com.example.expensetrackerapp.ui.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.auth.AuthManager;
import com.example.expensetrackerapp.auth.LoginActivity;
import com.example.expensetrackerapp.data.repository.UserRepository;
import com.example.expensetrackerapp.databinding.FragmentProfileBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.CurrencyUtils;
import com.example.expensetrackerapp.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Profile Fragment for user settings and account management.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthManager authManager;
    private UserRepository userRepository;
    private PreferenceManager preferenceManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = AuthManager.getInstance();
        userRepository = UserRepository.getInstance(requireContext());
        preferenceManager = PreferenceManager.getInstance(requireContext());

        setupUI();
        setupClickListeners();
        observeProfile();
    }

    private void setupUI() {
        // Set user info
        if (authManager.isGuest()) {
            binding.tvUserName.setText("Guest User");
            binding.tvUserEmail.setText("Not logged in");
            binding.cardGuestBanner.setVisibility(View.VISIBLE);
            binding.tvLogoutLabel.setText("Sign In");
            binding.btnDeleteAccount.setVisibility(View.GONE);
            binding.dividerDeleteAccount.setVisibility(View.GONE);
        } else {
            binding.tvUserName.setText(authManager.getCurrentUser().getDisplayName());
            binding.tvUserEmail.setText(authManager.getCurrentUserEmail());
            binding.cardGuestBanner.setVisibility(View.GONE);
            binding.tvLogoutLabel.setText(R.string.logout);
            binding.btnDeleteAccount.setVisibility(View.VISIBLE);
            binding.dividerDeleteAccount.setVisibility(View.VISIBLE);
        }

        // Set current theme selection
        int themeMode = preferenceManager.getThemeMode();
        switch (themeMode) {
            case Constants.THEME_LIGHT:
                binding.tvThemeValue.setText(R.string.light_mode);
                break;
            case Constants.THEME_DARK:
                binding.tvThemeValue.setText(R.string.dark_mode);
                break;
            default:
                binding.tvThemeValue.setText(R.string.system_default);
        }

        // Set current currency
        String currency = preferenceManager.getCurrency();
        binding.tvCurrencyValue.setText(currency);
    }

    private void setupClickListeners() {
        // Edit Profile
        binding.btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show();
        });

        // Currency setting
        binding.cardCurrency.setOnClickListener(v -> showCurrencyDialog());

        // Theme setting
        binding.cardTheme.setOnClickListener(v -> showThemeDialog());

        // Export data
        binding.cardExport.setOnClickListener(v -> Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show());

        // App lock
        binding.cardAppLock.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show();
        });

        // Logout
        binding.btnLogout.setOnClickListener(v -> {
            if (authManager.isGuest()) {
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            } else {
                showLogoutDialog();
            }
        });

        // Delete account
        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // Login from guest banner
        binding.btnGuestLogin.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void observeProfile() {
        userRepository.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                if (profile.getName() != null && !profile.getName().isEmpty()) {
                    binding.tvUserName.setText(profile.getName());
                }
            }
        });
    }

    private void showCurrencyDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_currency);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        RecyclerView rvCurrencies = dialog.findViewById(R.id.rvCurrencies);
        rvCurrencies.setLayoutManager(new LinearLayoutManager(requireContext()));

        String[] currencies = CurrencyUtils.getCurrencyDisplayNames();
        String[] currencyCodes = CurrencyUtils.getSupportedCurrencies();
        String currentCurrency = preferenceManager.getCurrency();

        CurrencyAdapter adapter = new CurrencyAdapter(currencyCodes, currencies, currentCurrency, code -> {
            preferenceManager.setCurrency(code);
            binding.tvCurrencyValue.setText(code);
            Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        rvCurrencies.setAdapter(adapter);
        dialog.show();
    }

    private void showThemeDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_theme);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroupTheme);
        RadioButton rbSystem = dialog.findViewById(R.id.rbSystemDefault);
        RadioButton rbLight = dialog.findViewById(R.id.rbLightMode);
        RadioButton rbDark = dialog.findViewById(R.id.rbDarkMode);

        int currentTheme = preferenceManager.getThemeMode();
        switch (currentTheme) {
            case Constants.THEME_LIGHT:
                rbLight.setChecked(true);
                break;
            case Constants.THEME_DARK:
                rbDark.setChecked(true);
                break;
            default:
                rbSystem.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int themeMode;
            String themeName;
            
            if (checkedId == R.id.rbLightMode) {
                themeMode = Constants.THEME_LIGHT;
                themeName = getString(R.string.light_mode);
            } else if (checkedId == R.id.rbDarkMode) {
                themeMode = Constants.THEME_DARK;
                themeName = getString(R.string.dark_mode);
            } else {
                themeMode = Constants.THEME_SYSTEM;
                themeName = getString(R.string.system_default);
            }

            preferenceManager.setThemeMode(themeMode);
            binding.tvThemeValue.setText(themeName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showLogoutDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);

        // Setup dialog content
        MaterialCardView iconContainer = dialogView.findViewById(R.id.iconContainer);
        iconContainer.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_light));
        
        ImageView ivIcon = dialogView.findViewById(R.id.ivDialogIcon);
        ivIcon.setImageResource(R.drawable.ic_logout);
        ivIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(R.string.confirm_logout_title);

        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
        tvMessage.setText(R.string.confirm_logout_message);

        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        btnConfirm.setText(R.string.yes);
        btnConfirm.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary));

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
                .create();

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnCancel.setText(R.string.no);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            authManager.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        dialog.show();
    }

    private void showDeleteAccountDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);

        // Setup dialog content - keep red/error colors for delete action
        ImageView ivIcon = dialogView.findViewById(R.id.ivDialogIcon);
        ivIcon.setImageResource(R.drawable.ic_delete);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        tvTitle.setText(R.string.confirm_delete_account_title);

        TextView tvMessage = dialogView.findViewById(R.id.tvDialogMessage);
        tvMessage.setText(R.string.confirm_delete_account_message);

        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirm);
        btnConfirm.setText(R.string.delete);

        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
                .create();

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            authManager.deleteAccount().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                } else {
                    Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    // Inner class for Currency Adapter
    private static class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {
        private final String[] codes;
        private final String[] displayNames;
        private final String selectedCode;
        private final OnCurrencySelectedListener listener;

        interface OnCurrencySelectedListener {
            void onSelected(String code);
        }

        CurrencyAdapter(String[] codes, String[] displayNames, String selectedCode, OnCurrencySelectedListener listener) {
            this.codes = codes;
            this.displayNames = displayNames;
            this.selectedCode = selectedCode;
            this.listener = listener;
        }

        @NonNull
        @Override
        public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_currency, parent, false);
            return new CurrencyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
            String code = codes[position];
            String displayName = displayNames[position];

            // Set symbol based on currency code
            String symbol;
            switch (code) {
                case Constants.CURRENCY_BDT: symbol = "৳"; break;
                case Constants.CURRENCY_USD: symbol = "$"; break;
                case Constants.CURRENCY_INR: symbol = "₹"; break;
                case Constants.CURRENCY_EUR: symbol = "€"; break;
                case Constants.CURRENCY_GBP: symbol = "£"; break;
                default: symbol = "$";
            }

            // Extract name (e.g. "Bangladeshi Taka" from "৳ BDT - Bangladeshi Taka")
            String name = displayName;
            if (displayName.contains("-")) {
                name = displayName.substring(displayName.indexOf("-") + 1).trim();
            }

            holder.tvSymbol.setText(symbol);
            holder.tvCode.setText(code);
            holder.tvName.setText(name);
            holder.ivSelected.setVisibility(code.equals(selectedCode) ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(v -> listener.onSelected(code));
        }

        @Override
        public int getItemCount() {
            return codes.length;
        }

        static class CurrencyViewHolder extends RecyclerView.ViewHolder {
            TextView tvSymbol, tvCode, tvName;
            ImageView ivSelected;

            CurrencyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSymbol = itemView.findViewById(R.id.tvCurrencySymbol);
                tvCode = itemView.findViewById(R.id.tvCurrencyCode);
                tvName = itemView.findViewById(R.id.tvCurrencyName);
                ivSelected = itemView.findViewById(R.id.ivSelected);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
