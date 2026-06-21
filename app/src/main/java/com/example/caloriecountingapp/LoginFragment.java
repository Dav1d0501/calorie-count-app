package com.example.caloriecountingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.caloriecountingapp.databinding.FragmentLoginBinding;

// Login screen. For now navigates to Goal; Firebase Auth wired in a later step.
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Temporary: skip real auth, just go to the Goal screen
        binding.loginButton.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_login_to_goal));

        binding.signupButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Sign up coming soon", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}