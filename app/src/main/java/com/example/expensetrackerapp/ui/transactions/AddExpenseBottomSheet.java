package com.example.expensetrackerapp.ui.transactions;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.data.local.AppDatabase;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.data.repository.ExpenseRepository;
import com.example.expensetrackerapp.databinding.BottomSheetAddExpenseBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.CurrencyUtils;
import com.example.expensetrackerapp.utils.DateUtils;
import com.example.expensetrackerapp.utils.PreferenceManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Calendar;

/**
 * Bottom Sheet for adding or editing expenses/income.
 */
public class AddExpenseBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddExpenseBinding binding;
    private ExpenseRepository expenseRepository;
    private PreferenceManager preferenceManager;

    private long expenseId = -1;
    private Expense existingExpense = null;
    private long selectedDate = System.currentTimeMillis();
    private boolean isExpenseType = true;

    private static final String ARG_EXPENSE_ID = "expense_id";

    public static AddExpenseBottomSheet newInstance(long expenseId) {
        AddExpenseBottomSheet fragment = new AddExpenseBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_EXPENSE_ID, expenseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            expenseId = getArguments().getLong(ARG_EXPENSE_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseRepository = ExpenseRepository.getInstance(requireContext());
        preferenceManager = PreferenceManager.getInstance(requireContext());


        setupDatePicker();
        setupTypeToggle();
        setupButtons();

        // If editing, load existing expense
        if (expenseId != -1) {
            loadExistingExpense();
        } else {
            updateDateDisplay();
        }
    }

    private void setupCategoryDropdown(boolean isExpense) {
        String[] categories;
        if (isExpense) {
            categories = new String[]{
                    Constants.CATEGORY_FOOD,
                    Constants.CATEGORY_TRANSPORT,
                    Constants.CATEGORY_SHOPPING,
                    Constants.CATEGORY_BILLS,
                    Constants.CATEGORY_ENTERTAINMENT,
                    Constants.CATEGORY_HEALTHCARE,
                    Constants.CATEGORY_EDUCATION,
                    Constants.CATEGORY_OTHERS
            };
        } else {
            categories = new String[]{
                    Constants.CATEGORY_SALARY,
                    Constants.CATEGORY_BUSINESS,
                    Constants.CATEGORY_INVESTMENT,
                    Constants.CATEGORY_FREELANCE,
                    Constants.CATEGORY_GIFT,
                    Constants.CATEGORY_RENTAL,
                    Constants.CATEGORY_REFUND,
                    Constants.CATEGORY_OTHERS
            };
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories);
        binding.actvCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.tilDate.setEndIconOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(selectedDate)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = selection;
            updateDateDisplay();
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void updateDateDisplay() {
        binding.etDate.setText(DateUtils.formatDate(selectedDate));
    }

    private void setupTypeToggle() {
        binding.toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                boolean newTypeIsExpense = checkedId == R.id.btnExpense;
                if (isExpenseType != newTypeIsExpense) {
                    isExpenseType = newTypeIsExpense;
                    setupCategoryDropdown(isExpenseType);
                    binding.actvCategory.setText("", false); // Clear invalid category
                    updateTitle();
                }
            }
        });

        // Default to expense
        binding.btnExpense.setChecked(true);
        setupCategoryDropdown(true); // Initial setup
    }

    private void updateTitle() {
        if (expenseId != -1) {
            binding.tvTitle.setText(isExpenseType ? R.string.edit_expense : R.string.edit_income);
        } else {
            binding.tvTitle.setText(isExpenseType ? R.string.add_expense : R.string.add_income);
        }
    }

    private void setupButtons() {
        binding.btnSave.setOnClickListener(v -> saveExpense());
        binding.btnCancel.setOnClickListener(v -> dismiss());

        if (expenseId != -1) {
            binding.btnDelete.setVisibility(View.VISIBLE);
            binding.btnDelete.setOnClickListener(v -> deleteExpense());
        } else {
            binding.btnDelete.setVisibility(View.GONE);
        }
    }

    private void loadExistingExpense() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            existingExpense = AppDatabase.getInstance(requireContext())
                    .expenseDao().getExpenseByIdSync(expenseId);

            if (existingExpense != null) {
                requireActivity().runOnUiThread(() -> {
                    selectedDate = existingExpense.getDate();
                    updateDateDisplay();

                    isExpenseType = Constants.TYPE_EXPENSE.equals(existingExpense.getType());
                    setupCategoryDropdown(isExpenseType); // Manually update adapter based on type
                    
                    if (isExpenseType) {
                        if (!binding.btnExpense.isChecked()) binding.btnExpense.setChecked(true);
                    } else {
                        if (!binding.btnIncome.isChecked()) binding.btnIncome.setChecked(true);
                    }

                    // Populate fields AFTER adapter is ready
                    binding.etAmount.setText(String.valueOf(existingExpense.getAmount()));
                    binding.actvCategory.setText(existingExpense.getCategory(), false);
                    binding.etNotes.setText(existingExpense.getNotes());

                    updateTitle();
                });
            }
        });
    }

    private void saveExpense() {
        // Validate inputs
        String amountStr = binding.etAmount.getText().toString().trim();
        String category = binding.actvCategory.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            binding.tilAmount.setError(getString(R.string.error_amount_required));
            return;
        }
        binding.tilAmount.setError(null);

        if (TextUtils.isEmpty(category)) {
            binding.tilCategory.setError(getString(R.string.error_category_required));
            return;
        }
        binding.tilCategory.setError(null);

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            binding.tilAmount.setError("Invalid amount");
            return;
        }

        // Create or update expense
        Expense expense;
        if (existingExpense != null) {
            expense = existingExpense;
        } else {
            expense = new Expense();
            expense.setCreatedAt(System.currentTimeMillis());
        }

        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(selectedDate);
        expense.setNotes(notes);
        expense.setType(isExpenseType ? Constants.TYPE_EXPENSE : Constants.TYPE_INCOME);

        // Show loading
        binding.btnSave.setEnabled(false);

        if (existingExpense != null) {
            expenseRepository.update(expense, new ExpenseRepository.OnOperationCompleteListener() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), R.string.transaction_updated, Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }

                @Override
                public void onFailure(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        binding.btnSave.setEnabled(true);
                    });
                }
            });
        } else {
            expenseRepository.insert(expense, new ExpenseRepository.OnOperationCompleteListener() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), R.string.transaction_added, Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }

                @Override
                public void onFailure(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                        binding.btnSave.setEnabled(true);
                    });
                }
            });
        }
    }

    private void deleteExpense() {
        if (existingExpense == null)
            return;

        expenseRepository.delete(existingExpense, new ExpenseRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), R.string.transaction_deleted, Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }

            @Override
            public void onFailure(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
