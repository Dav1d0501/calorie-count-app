package com.example.caloriecountingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.caloriecountingapp.data.FirestoreRepository;
import com.example.caloriecountingapp.databinding.FragmentLoginBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

// Login screen: email/password sign-in and sign-up via Firebase Auth.
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth auth;
    private final FirestoreRepository repository = new FirestoreRepository();
    private UserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // If already signed in from a previous session, route immediately
        if (auth.getCurrentUser() != null) {
            route(view);
            return;
        }

        binding.loginButton.setOnClickListener(v -> signIn(view));
        binding.signupButton.setOnClickListener(v -> signUp(view));
    }

    // Validate the email/password inputs
    private boolean validate() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Enter email and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void signIn(View view) {
        if (!validate()) return;
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> route(view))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Sign in failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void signUp(View view) {
        if (!validate()) return;
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Toast.makeText(getContext(), "Account created", Toast.LENGTH_SHORT).show();
                    route(view);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Sign up failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // Decide where to go: if the user already has a saved target, skip Goal.
    private void route(View view) {
        repository.loadTarget(target -> {
            if (target > 0) {
                viewModel.setDailyTarget(target);   // load saved target into shared state
                Navigation.findNavController(view).navigate(R.id.action_login_to_summary);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_login_to_goal);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}