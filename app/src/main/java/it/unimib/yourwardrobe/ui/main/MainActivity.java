package it.unimib.yourwardrobe.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            MaterialToolbar toolbar = findViewById(R.id.top_bar);
            if (toolbar != null) {
                toolbar.setPadding(
                        toolbar.getPaddingLeft(),
                        systemBars.top,
                        toolbar.getPaddingRight(),
                        toolbar.getPaddingBottom()
                );
            }
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }

            return insets;
        });
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().
                findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            NavigationUI.setupWithNavController(bottomNav, navController);
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.addGarmentFragment) {
                    bottomNav.setVisibility(android.view.View.GONE);
                } else {
                    bottomNav.setVisibility(android.view.View.VISIBLE);
                }
            });
            MaterialToolbar toolbar = findViewById(R.id.top_bar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.homeFragment);
            topLevelDestinations.add(R.id.wardrobeFragment);
            topLevelDestinations.add(R.id.personalStylistFragment);
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}