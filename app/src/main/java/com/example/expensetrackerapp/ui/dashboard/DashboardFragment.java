package com.example.expensetrackerapp.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.auth.AuthManager;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.data.repository.ExpenseRepository;
import com.example.expensetrackerapp.data.repository.UserRepository;
import com.example.expensetrackerapp.databinding.FragmentDashboardBinding;
import com.example.expensetrackerapp.ui.transactions.TransactionAdapter;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.CurrencyUtils;
import com.example.expensetrackerapp.utils.DateUtils;
import com.example.expensetrackerapp.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Fragment showing expense summary, balance, and recent transactions.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ExpenseRepository expenseRepository;
    private UserRepository userRepository;
    private PreferenceManager preferenceManager;
    private AuthManager authManager;
    private TransactionAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseRepository = ExpenseRepository.getInstance(requireContext());
        userRepository = UserRepository.getInstance(requireContext());
        preferenceManager = PreferenceManager.getInstance(requireContext());
        authManager = AuthManager.getInstance();

        setupUI();
        setupRecyclerView();
        observeData();
    }

    private void setupUI() {
        // Set greeting based on time
        String greeting = getGreeting();
        binding.tvGreeting.setText(greeting);

        // Set user name
        String userName = authManager.isGuest() ? "Guest"
                : (authManager.getCurrentUser() != null && authManager.getCurrentUser().getDisplayName() != null
                        ? authManager.getCurrentUser().getDisplayName()
                        : "User");
        binding.tvUserName.setText(userName);

        // Set current month
        binding.tvCurrentMonth.setText(DateUtils.formatMonthYear(System.currentTimeMillis()));
    }

    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return "Good Morning,";
        } else if (hour < 17) {
            return "Good Afternoon,";
        } else {
            return "Good Evening,";
        }
    }

    private void setupRecyclerView() {
        recentAdapter = new TransactionAdapter(requireContext(), new ArrayList<>(), expense -> {
            // Handle transaction click - open edit
        });

        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(recentAdapter);
        binding.rvRecentTransactions.setNestedScrollingEnabled(false);
    }

    private void observeData() {
        String currency = preferenceManager.getCurrency();
        long[] monthRange = DateUtils.getDateRangeForFilter(Constants.FILTER_MONTH);

        // Observe total expenses
        expenseRepository.getTotalExpenses(monthRange[0], monthRange[1]).observe(getViewLifecycleOwner(),
                totalExpense -> {
                    if (totalExpense != null) {
                        binding.tvTotalExpense.setText(CurrencyUtils.formatAmount(totalExpense, currency));
                        updateBalance();
                    }
                });

        // Observe total income
        expenseRepository.getTotalIncome(monthRange[0], monthRange[1]).observe(getViewLifecycleOwner(), totalIncome -> {
            if (totalIncome != null) {
                binding.tvTotalIncome.setText(CurrencyUtils.formatAmount(totalIncome, currency));
                updateBalance();
            }
        });

        // Observe recent transactions
        expenseRepository.getByDateRange(monthRange[0], monthRange[1]).observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                // Take only first 5 transactions
                List<Expense> recent = expenses.size() > 5 ? expenses.subList(0, 5) : expenses;
                recentAdapter.updateData(recent);
                binding.rvRecentTransactions.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
            } else {
                binding.rvRecentTransactions.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            }
        });

        // Observe monthly budget
        userRepository.getMonthlyBudget().observe(getViewLifecycleOwner(), budget -> {
            if (budget != null && budget > 0) {
                binding.cardBudget.setVisibility(View.VISIBLE);
                updateBudgetProgress(budget);
            } else {
                binding.cardBudget.setVisibility(View.GONE);
            }
        });
    }

    private void updateBalance() {
        String currency = preferenceManager.getCurrency();

        // Parse amounts from text views
        String expenseText = binding.tvTotalExpense.getText().toString();
        String incomeText = binding.tvTotalIncome.getText().toString();

        double expense = CurrencyUtils.parseAmount(expenseText);
        double income = CurrencyUtils.parseAmount(incomeText);

        double balance = income - expense;
        binding.tvBalance.setText(CurrencyUtils.formatAmount(balance, currency));

        // Set balance color
        if (balance >= 0) {
            binding.tvBalance.setTextColor(getResources().getColor(R.color.income, null));
        } else {
            binding.tvBalance.setTextColor(getResources().getColor(R.color.expense, null));
        }
    }

    private void updateBudgetProgress(double budget) {
        String currency = preferenceManager.getCurrency();
        String expenseText = binding.tvTotalExpense.getText().toString();
        double spent = CurrencyUtils.parseAmount(expenseText);

        double remaining = budget - spent;
        int percentage = budget > 0 ? (int) ((spent / budget) * 100) : 0;
        percentage = Math.min(percentage, 100);

        binding.tvBudgetAmount.setText(CurrencyUtils.formatAmount(budget, currency));
        binding.tvBudgetSpent.setText(CurrencyUtils.formatAmount(spent, currency) + " spent");
        binding.progressBudget.setProgress(percentage);

        if (remaining > 0) {
            binding.tvBudgetRemaining.setText(CurrencyUtils.formatAmount(remaining, currency) + " remaining");
            binding.tvBudgetRemaining.setTextColor(getResources().getColor(R.color.success, null));
        } else {
            binding.tvBudgetRemaining
                    .setText("Budget exceeded by " + CurrencyUtils.formatAmount(Math.abs(remaining), currency));
            binding.tvBudgetRemaining.setTextColor(getResources().getColor(R.color.error, null));
        }

        // Set progress color based on percentage
        if (percentage >= 100) {
            binding.progressBudget.setIndicatorColor(getResources().getColor(R.color.error, null));
        } else if (percentage >= 80) {
            binding.progressBudget.setIndicatorColor(getResources().getColor(R.color.warning, null));
        } else {
            binding.progressBudget.setIndicatorColor(getResources().getColor(R.color.primary, null));
        }
    }

    public void refreshData() {
        observeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
