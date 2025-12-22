package it.unimib.yourwardrobe.ui.main.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

import it.unimib.yourwardrobe.R;

public class CardWeather extends LinearLayout  {
    private TextView tvCondition;
    private TextView tvTemperature;
    private ConstraintLayout layout;

    public CardWeather(Context context) {
        super(context);
        init(context);
    }

    public CardWeather(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardWeather(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.card_weather, this);
        tvCondition = findViewById(R.id.tv_weather_condition);
        tvTemperature = findViewById(R.id.tv_weather_temperature);
        layout = findViewById(R.id.layout_weather);
    }

    public void SetBackgroundImage(Drawable drawable) {
        layout.setBackground(drawable);
    }

    public void setCondition(String condition) {
        tvCondition.setText(condition);
    }

    public void setConditionIcon(Drawable drawable) {
        tvCondition.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public void setTemperature(Float temperature) {
        String temp = String.format(Locale.getDefault(),"%.1f", temperature);
        tvTemperature.setText(temp);
    }

}
