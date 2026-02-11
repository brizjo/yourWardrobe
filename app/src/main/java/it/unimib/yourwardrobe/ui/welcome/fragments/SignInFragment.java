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

import com.google.android.material.textfield.TextInputEditText;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.welcome.components.LoginButton;
import it.unimib.yourwardrobe.ui.welcome.viewmodel.AuthViewModel;

@AndroidEntryPoint
public class SignInFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private LoginButton loginButton;
    private LoginButton signUpButton;
    private LoginButton googleButton;
    private long lastClickTime = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailEditText = view.findViewById(R.id.textInputEmail);
        passwordEditText = view.findViewById(R.id.textInputPassword);
        loginButton = view.findViewById(R.id.login_button);
        googleButton = view.findViewById(R.id.login_button_google);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        loginButton.setButtonText(getString(R.string.login));
        loginButton.setOnButtonClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();
            String email = emailEditText.getText() != null ? emailEditText.getText().toString() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

            authViewModel.signInWithEmail(email, password);
        });

        googleButton = view.findViewById(R.id.login_button_google);
        googleButton.setButtonText(getString(R.string.login_with_google));
        googleButton.setButtonIcon(R.drawable.ic_google);
        googleButton.setOnButtonClickListener(v -> {
            authViewModel.signInWithGoogle();
        });

        signUpButton = view.findViewById(R.id.sign_up_button);
        signUpButton.setButtonText(getString(R.string.sign_up));
        signUpButton.setOnButtonClickListener(v -> {
            navigateToSignUpFragment();
        });

    }

    private void navigateToSignUpFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_signUpFragment);
    }
}
