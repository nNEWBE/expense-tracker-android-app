package com.example.expensetrackerapp.ui.transactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.CurrencyUtils;
import com.example.expensetrackerapp.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying transactions with modern UI.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private List<Expense> expenses;
    private final OnTransactionClickListener listener;
    private final PreferenceManager preferenceManager;

    public interface OnTransactionClickListener {
        void onTransactionClick(Expense expense);
    }

    public TransactionAdapter(Context context, List<Expense> expenses, OnTransactionClickListener listener) {
        this.context = context;
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        this.listener = listener;
        this.preferenceManager = PreferenceManager.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        String currency = preferenceManager.getCurrency();

        // Title: Use Notes if available (e.g., Merchant Name), else Category
        String title = (expense.getNotes() != null && !expense.getNotes().isEmpty())
                ? expense.getNotes()
                : expense.getCategory();
        holder.tvDescription.setText(title);

        // Subtitle: Category and Date? Just Category for now as per design
        holder.tvCategory.setText(expense.getCategory());

        // Icon
        holder.ivCategoryIcon.setImageResource(getCategoryIconResource(expense.getCategory()));

        // Amount formatting
        boolean isExpense = Constants.TYPE_EXPENSE.equals(expense.getType());
        String formattedAmount = CurrencyUtils.formatAmountWithSign(expense.getAmount(), currency, isExpense);
        holder.tvAmount.setText(formattedAmount);

        // Use Black color for all amounts as per the modern design image
        holder.tvAmount.setTextColor(context.getColor(R.color.text_primary));

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateData(List<Expense> newExpenses) {
        final List<Expense> finalNewExpenses = newExpenses != null ? newExpenses : new ArrayList<>();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return expenses.size();
            }

            @Override
            public int getNewListSize() {
                return finalNewExpenses.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return expenses.get(oldItemPosition).getId() == finalNewExpenses.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Expense oldItem = expenses.get(oldItemPosition);
                Expense newItem = finalNewExpenses.get(newItemPosition);
                return oldItem.getAmount() == newItem.getAmount()
                        && oldItem.getCategory().equals(newItem.getCategory())
                        && oldItem.getDate() == newItem.getDate();
            }
        });

        this.expenses = new ArrayList<>(finalNewExpenses);
        diffResult.dispatchUpdatesTo(this);
    }

    private int getCategoryIconResource(String category) {
        // Return appropriate vector drawable based on category
        switch (category) {
            case Constants.CATEGORY_FOOD:
                return R.drawable.ic_restaurant;
            case Constants.CATEGORY_TRANSPORT:
                return R.drawable.ic_transport;
            case Constants.CATEGORY_SHOPPING:
                return R.drawable.ic_shopping_bag;
            case Constants.CATEGORY_BILLS:
                return R.drawable.ic_list; // Fallback or dedicated icon
            case Constants.CATEGORY_ENTERTAINMENT:
                return R.drawable.ic_transport; // Placeholder
            case Constants.CATEGORY_HEALTHCARE:
                return R.drawable.ic_list;
            default:
                return R.drawable.ic_list;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvDescription;
        TextView tvCategory;
        TextView tvAmount;

        ViewHolder(View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
