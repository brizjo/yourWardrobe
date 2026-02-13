package it.unimib.yourwardrobe.ui.welcome.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.shared.AuthViewModel;

public class SignUpFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private TextInputEditText usernameEditText;
    private long lastClickTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        emailEditText = view.findViewById(R.id.signUpEmail);
        usernameEditText = view.findViewById(R.id.username_signup);
        passwordEditText = view.findViewById(R.id.signUpPassword);
        confirmPasswordEditText = view.findViewById(R.id.signUpConfirmPassword);
        MaterialButton signUpButton = view.findViewById(R.id.btn_sign_up);
        MaterialButton signInButton = view.findViewById(R.id.btn_go_sign_in);

        signUpButton.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();

            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
            String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";
            String username = usernameEditText.getText() != null ? usernameEditText.getText().toString().trim() : "";

            authViewModel.signUp(username, email, password, confirmPassword);
        });

        signInButton.setOnClickListener(v -> {
            navigateToSignInFragment();
        });
    }

    private void navigateToSignInFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.action_signUpFragment_to_loginFragment);
    }
}