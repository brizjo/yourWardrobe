package it.unimib.yourwardrobe.ui.main.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import it.unimib.yourwardrobe.R;

public class CardWeather extends LinearLayout {
    private TextView tvCondition;
    private TextView tvClock;
    private TextView tvDate;
    private ImageView ivConditionIcon;
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
        ivConditionIcon = findViewById(R.id.iv_weather_condition_icon);
        tvTemperature = findViewById(R.id.tv_weather_temperature);
        tvClock = findViewById(R.id.tv_weather_time);
        tvDate = findViewById(R.id.tv_date);
        layout = findViewById(R.id.layout_weather);
    }

    public void setConditionBackground(int drawableResourceId) {
        Glide.with(getContext())
                .asBitmap()
                .load(drawableResourceId)
                .error(R.drawable.mist)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        // Imposta il background tramite Drawable
                        Glide.with(getContext())
                                .load(drawableResourceId)
                                .error(R.drawable.mist)
                                .into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        layout.setBackground(resource);
                                    }
                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        layout.setBackground(placeholder);
                                    }
                                });

                        Palette.from(bitmap).generate(palette -> {
                            int dominant = palette.getDominantColor(Color.WHITE);
                            double r = Color.red(dominant) / 255.0;
                            double g = Color.green(dominant) / 255.0;
                            double b = Color.blue(dominant) / 255.0;
                            double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
                            int textColor = luminance > 0.5 ? Color.BLACK : Color.WHITE;
                            tvTemperature.setTextColor(textColor);
                            tvCondition.setTextColor(textColor);
                            tvClock.setTextColor(textColor);
                            tvDate.setTextColor(textColor);

                        });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    public void setCondition(String condition) {
        tvCondition.setText(condition);
    }

    public void setConditionIcon(String iconUrl) {
        Glide.with(getContext())
                .load(iconUrl)
                .error(R.drawable.ic_cloudy)
                .into(ivConditionIcon);
    }

    public void setTemperature(String temperature) {
        tvTemperature.setText(temperature);
    }
}