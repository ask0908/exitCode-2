package com.psj.welfare.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class MyXAxisValueFormatter extends ValueFormatter implements IAxisValueFormatter
{
    private String[] mValues;

    public MyXAxisValueFormatter()
    {
    }

    public MyXAxisValueFormatter(String[] values) {
        this.mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis)
    {
        return null;
    }

    public ArrayList<BarEntry> getDataSet() {

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();

        BarEntry v1e2 = new BarEntry(1, 4000f);
        valueSet1.add(v1e2);
        BarEntry v1e3 = new BarEntry(2, 3121f);
        valueSet1.add(v1e3);
        BarEntry v1e4 = new BarEntry(3, 5521f);
        valueSet1.add(v1e4);
        BarEntry v1e5 = new BarEntry(4, 10421f);
        valueSet1.add(v1e5);
        BarEntry v1e6 = new BarEntry(5, 27934f);
        valueSet1.add(v1e6);

        return valueSet1;
    }

}
