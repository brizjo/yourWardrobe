package it.unimib.yourwardrobe.ui.main.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import it.unimib.yourwardrobe.R;

public class CardComponent extends LinearLayout {

    private MaterialCardView materialCardView;

    public CardComponent(Context context){
        super(context, null);
    }

    public CardComponent(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardComponent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 1. Inflate the layout (button_component.xml) and attach it to this class
        LayoutInflater.from(context).inflate(R.layout.card_component, this, true);

        // 2. Find the button ID defined in your XML
        materialCardView = findViewById(R.id.card);
    }
    public void setCardText(String text) {
        if (materialCardView != null) {
            TextView textView = materialCardView.findViewById(R.id.text_view);
            textView.setText(text);
        }
    }

    public void setCardTextStyle(int font) {
        if (materialCardView != null) {
            TextView textView = materialCardView.findViewById(R.id.text_view);
            textView.setTextAppearance(font);
        }
    }
    public void setCardStroke(int color, int width) {
        if (materialCardView != null) {
            materialCardView.setStrokeColor(color);
            materialCardView.setStrokeWidth(width);
        }
    }
    public void setCardImage(Drawable drawable) {
        if (materialCardView != null) {
           materialCardView.setBackgroundDrawable(drawable);
        }
    }

    private void setCardClickable(){
        if (materialCardView != null) {
            materialCardView.setClickable(true);
        }
    }
    public void setOnCardClickListener(OnClickListener listener) {
        if (materialCardView != null) {
            materialCardView.setClickable(true);
            materialCardView.setOnClickListener(listener);
        }
    }
}
