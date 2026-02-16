package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.shared.AuthViewModel;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvEmail;
    private MaterialButton btnLogout;
    private AuthViewModel authViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate del layout per questo fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void loadUserData() {

        authViewModel
                .getAuthResult()
                .observe(getViewLifecycleOwner(), result -> {
                    switch (result.status) {
                        case LOADING:
                            break;
                        case SUCCESS:
                            if (result.data != null) {
                                tvUsername.setText(result.data.getDisplayName());
                                tvEmail.setText(result.data.getEmail());

                                Glide.with(this)
                                        .load(result.data.getPhotoUrl())
                                        .error(R.drawable.profile_avatar_placeholder)
                                        .into(ivAvatar);
                            }
                        case ERROR:
                    }
                });

    }

    private void performLogout() {
        authViewModel.signOut();
    }
}