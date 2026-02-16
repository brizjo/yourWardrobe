package it.unimib.yourwardrobe.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.core.functional.Result;
import it.unimib.yourwardrobe.ui.shared.AuthViewModel;
import it.unimib.yourwardrobe.ui.welcome.WelcomeActivity;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_bar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setUpObservers();
        setupNavigation();
    }

    public void setUpObservers() {
        authViewModel.getAuthResult()
                .observe(this, result -> {
                    if (result.status == Result.Status.SUCCESS && result.data == null) {
                        // user signed out so redirect to WelcomeActivity.class
                        navigateToWelcomeActivity();
                    }
                });

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

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().
                findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
            MaterialToolbar toolbar = findViewById(R.id.top_bar);

            NavigationUI.setupWithNavController(bottomNav, navController);

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.homeFragment);
            topLevelDestinations.add(R.id.wardrobeFragment);
            topLevelDestinations.add(R.id.personalStylistFragment);
            topLevelDestinations.add(R.id.profileFragment);
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
            setSupportActionBar(toolbar);
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }


            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                toolbar.setTitle(null);

                if (destination.getId() == R.id.addGarmentFragment ||
                        destination.getId() == R.id.garmentFragment ||
                        destination.getId() == R.id.createOutfitFragment ||
                        destination.getId() == R.id.singleOutfitFragment) {
                    bottomNav.setVisibility(android.view.View.GONE);
                } else {
                    bottomNav.setVisibility(android.view.View.VISIBLE);
                }

                boolean isTopLevelDestination = topLevelDestinations.contains(destination.getId());
                if (!isTopLevelDestination) {
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                }
            });
            bottomNav.setOnItemReselectedListener(item -> {
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(item.getItemId(), true)
                        .setLaunchSingleTop(true)
                        .build();
                navController.navigate(item.getItemId(), null, navOptions);
            });

        }
    }


    private void navigateToWelcomeActivity() {
        var intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

}