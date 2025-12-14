package it.unimib.yourwardrobe.ui.welcome.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import it.unimib.yourwardrobe.R;

public class LoginButton extends LinearLayout {
    private MaterialButton materialButton;

    public LoginButton(Context context){
        super(context, null);
    }

    public LoginButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoginButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 1. Inflate the layout (button_component.xml) and attach it to this class
        LayoutInflater.from(context).inflate(R.layout.button_component, this, true);

        // 2. Find the button ID defined in your XML
        materialButton = findViewById(R.id.button);
        materialButton.setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_onPrimaryContainer));
    }

    public void setButtonText(String text) {
        if (materialButton != null) {
            materialButton.setText(text);
        }
    }
    public void setButtonIcon(int drawable) {
        if (materialButton != null) {
            materialButton.setIconResource(drawable);
        }
    }
    public void setOnButtonClickListener(OnClickListener listener) {
        if (materialButton != null) {
            materialButton.setOnClickListener(listener);
        }
    }
}
