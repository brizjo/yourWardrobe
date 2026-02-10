package it.unimib.yourwardrobe.ui.welcome;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.MainActivity;
import it.unimib.yourwardrobe.ui.welcome.viewmodel.AuthViewModel;

@AndroidEntryPoint
public class WelcomeActivity extends AppCompatActivity {

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

        var authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        authViewModel.authResult
                .observe(this, result -> {
                    navigateToMainActivity();
                    navigateToMainActivity();
                });

        authViewModel.getCurrentUser();
    }

    private void navigateToMainActivity() {
        var intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}