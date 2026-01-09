package com.example.expensetrackerapp.ui.dashboard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private CategoryAdapter categoryAdapter;
    
    private List<Expense> allExpenses = new ArrayList<>();
    private String currentCategory = "All";

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
        // Transactions Recycler
        recentAdapter = new TransactionAdapter(requireContext(), new ArrayList<>(), expense -> {
            // Handle transaction click - open edit
        });
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(recentAdapter);
        binding.rvRecentTransactions.setNestedScrollingEnabled(false);

        // Category Filter Recycler
        List<CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryItem("All", R.drawable.ic_list));
        
        categoryAdapter = new CategoryAdapter(requireContext(), categories, category -> {
            currentCategory = category;
            filterTransactions();
        });
        binding.rvCategoryFilters.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategoryFilters.setAdapter(categoryAdapter);
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
            if (expenses != null) {
                allExpenses = expenses;
                updateCategories(expenses); // Dynamic categories
                filterTransactions();
            } else {
                allExpenses = new ArrayList<>();
                updateCategories(new ArrayList<>());
                filterTransactions();
            }
        });
    }

    private void updateCategories(List<Expense> expenses) {
        List<CategoryItem> categoryItems = new ArrayList<>();
        // Always add "All" first
        categoryItems.add(new CategoryItem("All", R.drawable.ic_list));

        // Get unique categories using Stream API
        List<String> uniqueCategories = expenses.stream()
                .map(Expense::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (String category : uniqueCategories) {
            categoryItems.add(new CategoryItem(category, getCategoryIcon(category)));
        }

        if (categoryAdapter != null) {
            categoryAdapter.updateData(categoryItems);
        }
    }

    private int getCategoryIcon(String category) {
        switch (category) {
            case Constants.CATEGORY_FOOD: return R.drawable.ic_restaurant;
            case Constants.CATEGORY_TRANSPORT: return R.drawable.ic_transport;
            case Constants.CATEGORY_SHOPPING: return R.drawable.ic_shopping_bag;
            case Constants.CATEGORY_BILLS: return R.drawable.ic_receipt;
            case Constants.CATEGORY_ENTERTAINMENT: return R.drawable.ic_movie;
            case Constants.CATEGORY_HEALTHCARE: return R.drawable.ic_medical; 
            case Constants.CATEGORY_EDUCATION: return R.drawable.ic_school;
            case Constants.CATEGORY_SALARY: return R.drawable.ic_attach_money;
            case Constants.CATEGORY_INVESTMENT: return R.drawable.ic_trending_up;
            case Constants.CATEGORY_OTHERS: return R.drawable.ic_more_horiz;
            default: return R.drawable.ic_list;
        }
    }

    private void filterTransactions() {
        List<Expense> filtered;
        if (currentCategory.equals("All")) {
            filtered = new ArrayList<>(allExpenses);
        } else {
            filtered = allExpenses.stream()
                    .filter(e -> currentCategory.equalsIgnoreCase(e.getCategory()))
                    .collect(Collectors.toList());
        }

        // Only show top 5 of filtered results for the dashboard
        List<Expense> recent = filtered.size() > 5 ? filtered.subList(0, 5) : filtered;
        
        if (!recent.isEmpty()) {
            recentAdapter.updateData(recent);
            binding.rvRecentTransactions.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
        } else {
            binding.rvRecentTransactions.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        }
    }

    private void updateBalance() {
        String currency = preferenceManager.getCurrency();

        // Parse amounts from text views
        String expenseText = binding.tvTotalExpense.getText().toString();
        String incomeText = binding.tvTotalIncome.getText().toString();

        double expense = CurrencyUtils.parseAmount(expenseText);
        double income = CurrencyUtils.parseAmount(incomeText);

        double balance = income - expense;
        // Format with space between sign and amount for design: "- $66.00"
        String formattedBalance = CurrencyUtils.formatAmount(Math.abs(balance), currency);
        if (balance < 0) {
            binding.tvBalance.setText("- " + formattedBalance);
        } else {
            binding.tvBalance.setText(formattedBalance);
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
    
    // --- Helper Classes ---

    private static class CategoryItem {
        String name;
        int iconRes;

        CategoryItem(String name, int iconRes) {
            this.name = name;
            this.iconRes = iconRes;
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private Context context;
        private List<CategoryItem> items;
        private OnCategoryClickListener listener;
        private int selectedPosition = 0;

        CategoryAdapter(Context context, List<CategoryItem> items, OnCategoryClickListener listener) {
            this.context = context;
            this.items = items;
            this.listener = listener;
        }

        public void updateData(List<CategoryItem> newItems) {
            this.items = newItems;
            this.selectedPosition = 0; // Reset selection to All when data updates
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_category_filter, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CategoryItem item = items.get(position);
            boolean isSelected = position == selectedPosition;

            holder.tvName.setText(item.name);
            holder.ivIcon.setImageResource(item.iconRes);

            if (isSelected) {
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary));
                holder.card.setStrokeWidth(0);
                holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.white));
                holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.primary));
                holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
                holder.card.setStrokeWidth(2); // 1dp approx
                holder.ivIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
                holder.tvName.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                holder.tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            holder.itemView.setOnClickListener(v -> {
                int oldPos = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                listener.onCategoryClick(item.name);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView card;
            ImageView ivIcon;
            TextView tvName;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardCategory);
                ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
                tvName = itemView.findViewById(R.id.tvCategoryName);
            }
        }
    }

    interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }
}
