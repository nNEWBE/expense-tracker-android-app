package com.example.expensetrackerapp.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.data.repository.ExpenseRepository;
import com.example.expensetrackerapp.databinding.FragmentTransactionsBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.DateUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying and filtering all transactions.
 */
public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private ExpenseRepository expenseRepository;
    private TransactionAdapter adapter;
    private int currentFilter = Constants.FILTER_ALL;
    private String currentCategoryFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseRepository = ExpenseRepository.getInstance(requireContext());

        setupRecyclerView();
        setupFilterChips();
        observeTransactions();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(requireContext(), new ArrayList<>(), expense -> {
            // Open edit bottom sheet
            AddExpenseBottomSheet bottomSheet = AddExpenseBottomSheet.newInstance(expense.getId());
            bottomSheet.show(getParentFragmentManager(), "EditExpenseBottomSheet");
        });

        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void setupFilterChips() {
        // Date filter chips
        binding.chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = Constants.FILTER_ALL;
                observeTransactions();
            }
        });

        binding.chipToday.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = Constants.FILTER_TODAY;
                observeTransactions();
            }
        });

        binding.chipWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = Constants.FILTER_WEEK;
                observeTransactions();
            }
        });

        binding.chipMonth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentFilter = Constants.FILTER_MONTH;
                observeTransactions();
            }
        });

        // Category filter chips
        for (int i = 0; i < binding.chipGroupCategory.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupCategory.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentCategoryFilter = buttonView.getText().toString();
                    if (currentCategoryFilter.equals("All")) {
                        currentCategoryFilter = null;
                    }
                    observeTransactions();
                }
            });
        }
    }

    private void observeTransactions() {
        long[] dateRange = DateUtils.getDateRangeForFilter(currentFilter);

        if (currentCategoryFilter != null) {
            expenseRepository.getByCategoryAndDateRange(currentCategoryFilter, dateRange[0], dateRange[1])
                    .observe(getViewLifecycleOwner(), this::updateTransactionList);
        } else {
            expenseRepository.getByDateRange(dateRange[0], dateRange[1])
                    .observe(getViewLifecycleOwner(), this::updateTransactionList);
        }
    }

    private void updateTransactionList(List<Expense> expenses) {
        if (expenses != null && !expenses.isEmpty()) {
            adapter.updateData(expenses);
            binding.rvTransactions.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        } else {
            adapter.updateData(new ArrayList<>());
            binding.rvTransactions.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
