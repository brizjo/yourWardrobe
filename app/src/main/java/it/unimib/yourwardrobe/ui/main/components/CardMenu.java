package it.unimib.yourwardrobe.ui.main.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import it.unimib.yourwardrobe.R;

public class CardMenu extends LinearLayout {

    private MaterialCardView materialCardView;
    private ImageView imageView;

    public CardMenu(Context context){
        super(context, null);
    }

    public CardMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 1. Inflate the layout (button_component.xml) and attach it to this class
        LayoutInflater.from(context).inflate(R.layout.card_component, this, true);

        // 2. Find the button ID defined in your XML
        materialCardView = findViewById(R.id.card);
        imageView = findViewById(R.id.card_image);
        materialCardView.setCardBackgroundColor(ContextCompat.getColorStateList(context, R.color.md_theme_background));
    }
    public void setCardText(String text) {
        if (materialCardView != null) {
            TextView textView = materialCardView.findViewById(R.id.text_view);
            textView.setText(text);
            textView.setVisibility(VISIBLE);
        }
    }

    public void setCardTextStyle(int font, int color) {
        if (materialCardView != null) {
            TextView textView = materialCardView.findViewById(R.id.text_view);
            textView.setTextAppearance(font);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
            textView.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
            textView.setTextColor(color);
        }
    }
    public void setCardStroke(int color, int width) {
        if (materialCardView != null) {
            materialCardView.setStrokeColor(color);
            materialCardView.setStrokeWidth(width);
        }
    }
    public void setCardImage(Drawable drawable) {
        if(imageView != null){
            imageView.setImageDrawable(drawable);
        }
        if (materialCardView != null) {
           materialCardView.setClipToOutline(true);
           LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) materialCardView.getLayoutParams();
           params.height =(int) (150 * getResources().getDisplayMetrics().density);
           params.bottomMargin =(int) (16 * getResources().getDisplayMetrics().density);
           materialCardView.setLayoutParams(params);
        }
    }

    public void setOnCardClickListener(OnClickListener listener) {
        if (materialCardView != null) {
            materialCardView.setClickable(true);
            materialCardView.setOnClickListener(listener);
        }
    }
}
