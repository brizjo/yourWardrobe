package it.unimib.yourwardrobe.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
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
import it.unimib.yourwardrobe.ui.welcome.WelcomeActivity;
import it.unimib.yourwardrobe.ui.welcome.viewmodel.AuthViewModel;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

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

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupNavigation();

        var ivAvatar = (ImageView) findViewById(R.id.iv_avatar);
        var listPopupWindow = getListPopupWindow(ivAvatar);
        ivAvatar.setOnClickListener(v -> {
            listPopupWindow.show();
        });
    }

    @NonNull
    private ListPopupWindow getListPopupWindow(ImageView ivAvatar) {
        var listPopupWindow = new ListPopupWindow(this);

        String[] labels = {"Sign Out"};
        int[] icons = {R.drawable.ic_sign_out};

        var adapter = new ArrayAdapter<>(this, R.layout.profile_item_row, R.id.item_text, labels) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ImageView itemIcon = view.findViewById(R.id.item_icon);
                itemIcon.setImageResource(icons[position]);
                return view;
            }
        };

        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setAnchorView(ivAvatar);
        listPopupWindow.setWidth(500);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    authViewModel.signOut();
                    navigateToWelcomeActivity();
                }

                listPopupWindow.dismiss();
            }
        });

        return listPopupWindow;
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

    private void navigateToWelcomeActivity() {
        var intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

}