package com.channelsoft.sop.dataBinding;


import android.view.View;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.channelsoft.sop.R;


public class CustomDataBinding {
    /*------------------------------------------------------------------------order purpose-----------------------------------------------------------------*/
    @BindingAdapter("android:temperature")
    public static void setTemperatureColor(TextView textView, String temperature) {
        if (temperature != null) {
            try {
                double value = Double.parseDouble(temperature);
                if (value > 37.5)
                    textView.setTextColor(textView.getResources().getColor(R.color.warning));
                else if (value <= 37.5 && value >= 37) {
                    textView.setTextColor(textView.getResources().getColor(R.color.orange));
                } else textView.setTextColor(textView.getResources().getColor(R.color.green));
            } catch (Exception e) {
                textView.setVisibility(View.GONE);
            }
        }
    }

}
