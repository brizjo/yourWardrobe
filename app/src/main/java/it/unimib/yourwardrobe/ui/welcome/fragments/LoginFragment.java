package it.unimib.yourwardrobe.ui.welcome.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.ui.welcome.components.LoginButton;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
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
        LoginButton button = view.findViewById(R.id.login_button);
        button.setButtonText(getString(R.string.login));;
        LoginButton button_google = view.findViewById(R.id.login_button_google);
        button_google.setButtonText(getString(R.string.login_with_google));
        button_google.setButtonIcon(R.drawable.ic_google);
        LoginButton signUpButton = view.findViewById(R.id.sign_up_button);
        signUpButton.setButtonText(getString(R.string.sign_up));

        /*Button button = view.findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText inputEmail = view.findViewById(R.id.textInputEmail);
                TextInputEditText inputPassword = view.findViewById(R.id.textInputPassword);
            }
        });*/
    }
}