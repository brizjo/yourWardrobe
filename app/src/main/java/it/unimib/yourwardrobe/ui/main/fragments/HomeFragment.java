package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.model.User;
import it.unimib.yourwardrobe.repository.UserRepository;


public class HomeFragment extends Fragment {
    
    private UserRepository userRepository;

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = new UserRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView hi_user = view.findViewById(R.id.hi_user);
        
        User currentUser = userRepository.getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            hi_user.setText(getString(R.string.ciao) + currentUser.getDisplayName());
        } else if (currentUser != null && currentUser.getEmail() != null) {
            hi_user.setText(getString(R.string.ciao) + currentUser.getEmail());
        } else {
             hi_user.setText(R.string.ciao_guest);
        }
    }
}
