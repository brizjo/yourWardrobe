package it.unimib.yourwardrobe.ui.welcome.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.welcome.components.LoginButton;
import it.unimib.yourwardrobe.ui.welcome.viewmodel.LoginViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private long lastClickTime = 0;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        emailEditText = view.findViewById(R.id.textInputEmail);
        passwordEditText = view.findViewById(R.id.textInputPassword);
        LoginButton loginButton = view.findViewById(R.id.login_button);
        loginButton.setButtonText(getString(R.string.login));
        loginButton.setOnButtonClickListener(v ->{
            // prevenzione click ripetuti
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();
                String email = emailEditText.getText() != null ? emailEditText.getText().toString() : "";
                String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

                // Passa i dati al ViewModel
                loginViewModel.login(email, password);
        });
        loginViewModel.getAuthenticationResult().observe(getViewLifecycleOwner(), result -> {
            if (result.success) {
                // Login OK: Naviga verso la Home
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_mainActivity);
                ToastHelper.show(getContext(), "Login effettuato!", false);
            } else {
                // Login Fallito: Mostra errore
                ToastHelper.show(getContext(), result.errorMessage, false);
            }
        });
        LoginButton buttonGoogle = view.findViewById(R.id.login_button_google);
        buttonGoogle.setButtonText(getString(R.string.login_with_google));
        buttonGoogle.setButtonIcon(R.drawable.ic_google);
        LoginButton signUpButton = view.findViewById(R.id.sign_up_button);
        signUpButton.setButtonText(getString(R.string.sign_up));
        signUpButton.setOnButtonClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_signUpFragment);
        });

    }
}