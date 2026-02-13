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

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.shared.AuthViewModel;

@AndroidEntryPoint
public class SignInFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton signUpBtn;
    private MaterialButton signInWithGoogleBtn;
    private MaterialButton signInBtn;

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
        signInBtn = view.findViewById(R.id.btn_sign_in);
        signInWithGoogleBtn = view.findViewById(R.id.btn_sign_in_google);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        signInBtn.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();
            String email = emailEditText.getText() != null ? emailEditText.getText().toString() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

            authViewModel.signInWithEmail(email, password);
        });

        signInWithGoogleBtn = view.findViewById(R.id.btn_sign_in_google);
        signInWithGoogleBtn.setOnClickListener(v -> {
            authViewModel.signInWithGoogle();
        });

        signUpBtn = view.findViewById(R.id.btn_go_sign_up);
        signUpBtn.setOnClickListener(v -> {
            navigateToSignUpFragment();
        });

    }

    private void navigateToSignUpFragment() {
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_signUpFragment);
    }
}
