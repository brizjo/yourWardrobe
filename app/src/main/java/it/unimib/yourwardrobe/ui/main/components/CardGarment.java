package it.unimib.yourwardrobe.ui.main.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

import it.unimib.yourwardrobe.R;

public class CardGarment extends LinearLayout {
    private MaterialCardView materialCardView;
    private ImageView imageView;

    public CardGarment(Context context){
        super(context, null);
    }

    public CardGarment(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardGarment(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.card_garment, this, true);

        materialCardView = findViewById(R.id.card);
        imageView = findViewById(R.id.card_garment_image);
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
