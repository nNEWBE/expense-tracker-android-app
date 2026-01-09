package com.example.expensetrackerapp.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.data.repository.ExpenseRepository;
import com.example.expensetrackerapp.databinding.FragmentAnalyticsBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.CurrencyUtils;
import com.example.expensetrackerapp.utils.DateUtils;
import com.example.expensetrackerapp.utils.PreferenceManager;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Analytics Fragment showing expense charts and statistics.
 */
public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private ExpenseRepository expenseRepository;
    private PreferenceManager preferenceManager;
    private boolean isIncomeSelected = false; // False = Expense, True = Income

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expenseRepository = ExpenseRepository.getInstance(requireContext());
        preferenceManager = PreferenceManager.getInstance(requireContext());

        setupUI();
        setupPieChart();
        setupBarChart();
        loadChartData();
    }

    private void setupUI() {
        binding.tvDateRange.setText(DateUtils.formatMonthYear(System.currentTimeMillis()));

        binding.btnToggleExpense.setOnClickListener(v -> toggleType(false));
        binding.btnToggleIncome.setOnClickListener(v -> toggleType(true));
    }

    private void toggleType(boolean isIncome) {
        if (isIncomeSelected == isIncome) return;
        isIncomeSelected = isIncome;
        
        updateToggleUI();
        loadChartData();
    }

    private void updateToggleUI() {
        if (isIncomeSelected) {
            // Income Selected
            binding.btnToggleIncome.setBackgroundResource(R.drawable.bg_pill_income);
            binding.btnToggleIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            
            binding.btnToggleExpense.setBackground(null);
            binding.btnToggleExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            
            binding.tvTotalLabel.setText("Total Income");
            
            // Set pills to green/white theme based on design usage (assuming primary is green)
            // Actually, for better contrast, active pill is white background with primary text color
            // Inactive is transparent with white text
             binding.btnToggleIncome.setBackgroundResource(R.drawable.bg_pill_income); 
             binding.btnToggleIncome.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
             binding.btnToggleIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
             
             binding.btnToggleExpense.setBackground(null);
             binding.btnToggleExpense.setTextColor(Color.WHITE);

        } else {
            // Expense Selected
             binding.btnToggleExpense.setBackgroundResource(R.drawable.bg_pill_expense);
             binding.btnToggleExpense.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
             binding.btnToggleExpense.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
             
             binding.btnToggleIncome.setBackground(null);
             binding.btnToggleIncome.setTextColor(Color.WHITE);
             
             binding.tvTotalLabel.setText("Total Expense");
        }
    }

    private void setupPieChart() {
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setExtraOffsets(5, 10, 5, 5);
        binding.pieChart.setDragDecelerationFrictionCoef(0.95f);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.setTransparentCircleColor(Color.WHITE);
        binding.pieChart.setTransparentCircleAlpha(110);
        binding.pieChart.setHoleRadius(58f);
        binding.pieChart.setTransparentCircleRadius(61f);
        binding.pieChart.setDrawCenterText(false);
        binding.pieChart.setRotationAngle(0);
        binding.pieChart.setRotationEnabled(true);
        binding.pieChart.setHighlightPerTapEnabled(true);
        binding.pieChart.setEntryLabelColor(Color.WHITE);
        binding.pieChart.setEntryLabelTextSize(12f);
        
        binding.pieChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        binding.pieChart.setNoDataText("No data available");

        Legend legend = binding.pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(10f);
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }

    private void setupBarChart() {
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.setDrawBarShadow(false);
        binding.barChart.setFitBars(true);
        binding.barChart.setPinchZoom(false);
        binding.barChart.setDrawValueAboveBar(false);
        binding.barChart.setScaleEnabled(false);
        
        binding.barChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        binding.barChart.setNoDataText("No trend data available");
        
        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        
        YAxis leftAxis = binding.barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#1A000000")); // Light grid
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        leftAxis.setAxisMinimum(0f);
        
        binding.barChart.getAxisRight().setEnabled(false);

        Legend legend = binding.barChart.getLegend();
        legend.setEnabled(false);
    }

    private void loadChartData() {
        long[] monthRange = DateUtils.getDateRangeForFilter(Constants.FILTER_MONTH);
        
        expenseRepository.getByDateRange(monthRange[0], monthRange[1]).observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null) {
                String targetType = isIncomeSelected ? Constants.TYPE_INCOME : Constants.TYPE_EXPENSE;
                List<Expense> filteredExpenses = new ArrayList<>();
                
                for (Expense e : expenses) {
                    if (targetType.equals(e.getType())) {
                        filteredExpenses.add(e);
                    }
                }
                
                if (!filteredExpenses.isEmpty()) {
                    updateStatsCards(filteredExpenses);
                    updatePieChart(filteredExpenses);
                    updateBarChart(filteredExpenses);
                    
                    // Show charts
                    binding.pieChart.setVisibility(View.VISIBLE);
                    binding.barChart.setVisibility(View.VISIBLE);
                } else {
                    // Empty state logic - reset charts to empty
                    binding.tvTotalAmount.setText(CurrencyUtils.formatAmount(0, preferenceManager.getCurrency()));
                    binding.tvDailyAverage.setText(CurrencyUtils.formatAmount(0, preferenceManager.getCurrency()));
                    binding.tvMaxTransaction.setText(CurrencyUtils.formatAmount(0, preferenceManager.getCurrency()));
                    
                    binding.pieChart.clear();
                    binding.barChart.clear();
                }
            } else {
                 binding.pieChart.clear();
                 binding.barChart.clear();
            }
        });
    }

    private void updateStatsCards(List<Expense> expenses) {
        double total = 0;
        double max = 0;
        
        for (Expense e : expenses) {
            total += e.getAmount();
            if (e.getAmount() > max) {
                max = e.getAmount();
            }
        }
        
        // Average
        int days = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        // Or better, days passed so far in month to be more accurate? Standard is usually / 30 for monthly views or actual days.
        // Let's use days in month for "Daily Average" spread.
        double average = total / days;
        
        String currency = preferenceManager.getCurrency();
        binding.tvTotalAmount.setText(CurrencyUtils.formatAmount(total, currency));
        binding.tvDailyAverage.setText(CurrencyUtils.formatAmount(average, currency));
        binding.tvMaxTransaction.setText(CurrencyUtils.formatAmount(max, currency));
    }

    private void updatePieChart(List<Expense> expenses) {
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense e : expenses) {
            categoryTotals.put(e.getCategory(), categoryTotals.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(getChartColors());
        dataSet.setValueTextColor(Color.WHITE);
        
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        binding.pieChart.setData(data);
        binding.pieChart.setCenterText(isIncomeSelected ? "Income" : "Expense");
        binding.pieChart.setCenterTextSize(12f);
        binding.pieChart.setCenterTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        binding.pieChart.animateY(1000, Easing.EaseInOutQuad);
        binding.pieChart.invalidate();
    }

    private void updateBarChart(List<Expense> expenses) {
        // Group by day for accurate trend (1-30/31)
        Map<Integer, Float> dailyTotals = new TreeMap<>(); // Sorted
        int maxDay = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Init all days with 0
        for(int i=1; i<=maxDay; i++) {
            dailyTotals.put(i, 0f);
        }

        for (Expense e : expenses) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(e.getDate());
            int day = cal.get(Calendar.DAY_OF_MONTH);
            dailyTotals.put(day, dailyTotals.get(day) + (float)e.getAmount());
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Float> entry : dailyTotals.entrySet()) {
            entries.add(new BarEntry(entry.getKey(), entry.getValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Amount");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary));
        dataSet.setDrawValues(false); // Clean look

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.setData(data);
        binding.barChart.animateY(1000);
        binding.barChart.invalidate();
    }
    
    private ArrayList<Integer> getChartColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_1));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_2));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_3));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_4));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_5));
        colors.add(ContextCompat.getColor(requireContext(), R.color.chart_6));
        return colors;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
