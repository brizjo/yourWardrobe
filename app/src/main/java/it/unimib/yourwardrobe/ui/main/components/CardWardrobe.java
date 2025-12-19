package it.unimib.yourwardrobe.ui.main.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import it.unimib.yourwardrobe.R;

public class CardWardrobe  extends LinearLayout {
    private MaterialCardView materialCardView;

    public CardWardrobe(Context context){
        super(context, null);
    }

    public CardWardrobe(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardWardrobe(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 1. Inflate the layout (button_component.xml) and attach it to this class
        LayoutInflater.from(context).inflate(R.layout.card_component, this, true);

        // 2. Find the button ID defined in your XML
        materialCardView = findViewById(R.id.card);
    }

    public void setCardImage(Drawable drawable) {
        if (materialCardView != null) {
            materialCardView.setBackgroundDrawable(drawable);
        }
    }

    public void setOnCardClickListener(OnClickListener listener) {
        if (materialCardView != null) {
            materialCardView.setClickable(true);
            materialCardView.setOnClickListener(listener);
        }
    }
}
