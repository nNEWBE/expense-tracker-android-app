package com.example.expensetrackerapp.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expensetrackerapp.R;
import com.example.expensetrackerapp.data.local.AppDatabase;
import com.example.expensetrackerapp.data.local.entity.Expense;
import com.example.expensetrackerapp.data.repository.ExpenseRepository;
import com.example.expensetrackerapp.databinding.FragmentAnalyticsBinding;
import com.example.expensetrackerapp.utils.Constants;
import com.example.expensetrackerapp.utils.CurrencyUtils;
import com.example.expensetrackerapp.utils.DateUtils;
import com.example.expensetrackerapp.utils.PreferenceManager;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analytics Fragment showing expense charts and statistics.
 */
public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private ExpenseRepository expenseRepository;
    private PreferenceManager preferenceManager;

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

        setupPieChart();
        setupBarChart();
        setupLineChart();
        loadChartData();
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
        binding.pieChart.setDrawCenterText(true);
        binding.pieChart.setCenterText("Expenses");
        binding.pieChart.setCenterTextSize(16f);
        binding.pieChart.setRotationAngle(0);
        binding.pieChart.setRotationEnabled(true);
        binding.pieChart.setHighlightPerTapEnabled(true);

        Legend legend = binding.pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(10f);
    }

    private void setupBarChart() {
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.setDrawBarShadow(false);
        binding.barChart.setFitBars(true);
        binding.barChart.setPinchZoom(false);
        binding.barChart.setDrawValueAboveBar(true);

        binding.barChart.getXAxis().setDrawGridLines(false);
        binding.barChart.getAxisLeft().setDrawGridLines(true);
        binding.barChart.getAxisRight().setEnabled(false);

        Legend legend = binding.barChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void setupLineChart() {
        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.setDrawGridBackground(false);
        binding.lineChart.setTouchEnabled(true);
        binding.lineChart.setDragEnabled(true);
        binding.lineChart.setScaleEnabled(false);
        binding.lineChart.setPinchZoom(false);

        binding.lineChart.getXAxis().setDrawGridLines(false);
        binding.lineChart.getAxisLeft().setDrawGridLines(true);
        binding.lineChart.getAxisRight().setEnabled(false);

        Legend legend = binding.lineChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void loadChartData() {
        long[] monthRange = DateUtils.getDateRangeForFilter(Constants.FILTER_MONTH);

        // Load category-wise data for pie chart
        expenseRepository.getByDateRange(monthRange[0], monthRange[1]).observe(getViewLifecycleOwner(), expenses -> {
            if (expenses != null && !expenses.isEmpty()) {
                Map<String, Double> categoryTotals = new HashMap<>();

                for (Expense expense : expenses) {
                    if (Constants.TYPE_EXPENSE.equals(expense.getType())) {
                        String category = expense.getCategory();
                        double current = categoryTotals.getOrDefault(category, 0.0);
                        categoryTotals.put(category, current + expense.getAmount());
                    }
                }

                updatePieChart(categoryTotals);
                updateBarChart(expenses);
                updateLineChart(expenses);

                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.scrollContent.setVisibility(View.VISIBLE);
            } else {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.scrollContent.setVisibility(View.GONE);
            }
        });
    }

    private void updatePieChart(Map<String, Double> categoryTotals) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.chart_1, null));
        colors.add(getResources().getColor(R.color.chart_2, null));
        colors.add(getResources().getColor(R.color.chart_3, null));
        colors.add(getResources().getColor(R.color.chart_4, null));
        colors.add(getResources().getColor(R.color.chart_5, null));
        colors.add(getResources().getColor(R.color.chart_6, null));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        binding.pieChart.setData(data);
        binding.pieChart.highlightValues(null);
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad);
        binding.pieChart.invalidate();
    }

    private void updateBarChart(List<Expense> expenses) {
        // Group by week for monthly view
        Map<Integer, Float> weeklyTotals = new HashMap<>();

        for (Expense expense : expenses) {
            if (Constants.TYPE_EXPENSE.equals(expense.getType())) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(expense.getDate());
                int weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH);

                float current = weeklyTotals.getOrDefault(weekOfMonth, 0f);
                weeklyTotals.put(weekOfMonth, current + (float) expense.getAmount());
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            entries.add(new BarEntry(i, weeklyTotals.getOrDefault(i, 0f)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Weekly Expenses");
        dataSet.setColor(getResources().getColor(R.color.primary, null));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.setData(data);
        binding.barChart.animateY(1000);
        binding.barChart.invalidate();
    }

    private void updateLineChart(List<Expense> expenses) {
        // Group by day for weekly view
        Map<Integer, Float> dailyTotals = new HashMap<>();

        for (Expense expense : expenses) {
            if (Constants.TYPE_EXPENSE.equals(expense.getType())) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(expense.getDate());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

                float current = dailyTotals.getOrDefault(dayOfWeek, 0f);
                dailyTotals.put(dayOfWeek, current + (float) expense.getAmount());
            }
        }

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            entries.add(new Entry(i, dailyTotals.getOrDefault(i, 0f)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Daily Trend");
        dataSet.setColor(getResources().getColor(R.color.primary, null));
        dataSet.setCircleColor(getResources().getColor(R.color.primary, null));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.primary_light, null));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(dataSet);

        binding.lineChart.setData(data);
        binding.lineChart.animateX(1000);
        binding.lineChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
