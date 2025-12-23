package it.unimib.yourwardrobe.ui.welcome.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.main.MainActivity;
import it.unimib.yourwardrobe.ui.welcome.components.LoginButton;
import it.unimib.yourwardrobe.ui.welcome.viewmodel.LoginViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private long lastClickTime = 0;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        emailEditText = view.findViewById(R.id.signUpEmail);
        passwordEditText = view.findViewById(R.id.signUpPassword);
        confirmPasswordEditText = view.findViewById(R.id.signUpConfirmPassword);
        LoginButton signUpButton = view.findViewById(R.id.confirm_sign_up_button);
        signUpButton.setButtonText(getString(R.string.sign_up));
        signUpButton.setOnButtonClickListener(v -> {
            // prevenzione click ripetuti
            if (System.currentTimeMillis() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = System.currentTimeMillis();

            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
            String confirmPassword = confirmPasswordEditText.getText() != null ? confirmPasswordEditText.getText().toString().trim() : "";

            // Chiama il metodo signUp nel ViewModel
            loginViewModel.signUp(email, password, confirmPassword);
        });
        loginViewModel.getAuthenticationResult().observe(getViewLifecycleOwner(), result -> {


            if (result.success) {
                // Registrazione avvenuta con successo
                ToastHelper.show(getContext(), "Registrazione completata!", false);

                // Avvia MainActivity e pulisce lo stack di navigazione
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();

            } else if (!result.success) {
                // Mostra l'errore di validazione o registrazione
                String errorMessage = ((LoginViewModel.AuthenticationResult) result).errorMessage;
                ToastHelper.show(getContext(), errorMessage, true);
            }
        });
    }
}