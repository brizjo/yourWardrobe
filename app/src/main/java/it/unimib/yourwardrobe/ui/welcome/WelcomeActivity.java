package it.unimib.yourwardrobe.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.MainActivity;
import it.unimib.yourwardrobe.ui.shared.AuthViewModel;

@AndroidEntryPoint
public class WelcomeActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setUpObservers();
    }

    private void setUpObservers() {
        authViewModel.getAuthResult().observe(this, result -> {
            switch (result.status) {
                case SUCCESS:
                    if (result.data != null) {
                        navigateToMainActivity();
                    }
                    break;
                case ERROR:
                    View ctxView = findViewById(android.R.id.content);
                    Snackbar.make(ctxView, "Invalid Credentials", Snackbar.LENGTH_LONG).show();
                    break;
                case LOADING:
                    // TODO:
                    break;
            }
        });
    }

    private void navigateToMainActivity() {
        var intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}