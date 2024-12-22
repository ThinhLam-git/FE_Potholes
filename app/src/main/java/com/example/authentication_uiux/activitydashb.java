package com.example.authentication_uiux;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class activitydashb extends AppCompatActivity {

    private ArrayList barArrayList ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_activitydashb);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        BarChart barChart = findViewById(R.id.barChart);
        //getData();
        BarDataSet barDataSet = new BarDataSet(barArrayList, "Bar Data");
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(R.color.black);

        barDataSet.setValueTextSize(16f);
        barChart.getDescription().setEnabled(true);
        barChart.animateY(2000);

    }
    private void getData()
    {
        barArrayList = new ArrayList();
        barArrayList.add(new BarEntry(2f,10));
        barArrayList.add(new BarEntry(3f,20));
        barArrayList.add(new BarEntry(4f,30));
        barArrayList.add(new BarEntry(5f,40));
        barArrayList.add(new BarEntry(6f,50));
        barArrayList.add(new BarEntry(7f,60));
        barArrayList.add(new BarEntry(8f,70));

    }

}