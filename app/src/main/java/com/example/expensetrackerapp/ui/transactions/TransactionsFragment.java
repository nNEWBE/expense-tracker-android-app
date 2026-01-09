package com.example.expensetrackerapp.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.data.repository.ExpenseRepository;
import com.example.expensetrackerapp.databinding.FragmentTransactionsBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.DateUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fragment for displaying and filtering all transactions.
 */
public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private ExpenseRepository expenseRepository;
    private TransactionAdapter adapter;
    private int currentFilter = Constants.FILTER_ALL;
    private String currentCategoryFilter = null; // null means "All Categories"
    private List<Expense> allTransactions = new ArrayList<>();

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
        setupDateFilterChips();
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

    private void setupDateFilterChips() {
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
    }

    private void observeTransactions() {
        long[] dateRange = DateUtils.getDateRangeForFilter(currentFilter);

        expenseRepository.getByDateRange(dateRange[0], dateRange[1])
                .observe(getViewLifecycleOwner(), expenses -> {
                    if (expenses != null) {
                        allTransactions = new ArrayList<>(expenses);
                        populateCategoryChips(allTransactions);
                        applyFilters();
                    } else {
                        allTransactions.clear();
                        updateTransactionList(new ArrayList<>());
                    }
                });
    }

    private void populateCategoryChips(List<Expense> expenses) {
        binding.chipGroupCategory.removeAllViews();

        // Add "All Categories" chip
        Chip allChip = new Chip(requireContext());
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setCheckedIconVisible(false);
        allChip.setChipBackgroundColorResource(R.color.surface);
        allChip.setTextAppearance(R.style.TextAppearance_App_Chip);
        allChip.setChecked(currentCategoryFilter == null);
        allChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentCategoryFilter = null;
                applyFilters();
            }
        });
        binding.chipGroupCategory.addView(allChip);

        // Extract unique categories
        Set<String> categories = new HashSet<>();
        for (Expense e : expenses) {
            if (e.getCategory() != null && !e.getCategory().isEmpty()) {
                categories.add(e.getCategory());
            }
        }

        // Add a chip for each category
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColorResource(R.color.surface);
            chip.setTextAppearance(R.style.TextAppearance_App_Chip);
            chip.setChecked(category.equals(currentCategoryFilter));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    currentCategoryFilter = category;
                    applyFilters();
                }
            });
            binding.chipGroupCategory.addView(chip);
        }
    }

    private void applyFilters() {
        List<Expense> filtered;

        if (currentCategoryFilter == null) {
            filtered = new ArrayList<>(allTransactions);
        } else {
            filtered = allTransactions.stream()
                    .filter(e -> currentCategoryFilter.equalsIgnoreCase(e.getCategory()))
                    .collect(Collectors.toList());
        }

        updateTransactionList(filtered);
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
