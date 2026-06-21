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

// Email/password sign-in and sign-up via Firebase Auth
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

        if (auth.getCurrentUser() != null) {
            route(view);
            return;
        }

        binding.loginButton.setOnClickListener(v -> signIn(view));
        binding.signupButton.setOnClickListener(v -> signUp(view));
    }

    private boolean validate() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.enter_email_password), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(getContext(), getString(R.string.password_too_short), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), getString(R.string.sign_in_failed, e.getMessage()),
                                Toast.LENGTH_LONG).show());
    }

    private void signUp(View view) {
        if (!validate()) return;
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Toast.makeText(getContext(), getString(R.string.account_created), Toast.LENGTH_SHORT).show();
                    route(view);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), getString(R.string.sign_up_failed, e.getMessage()),
                                Toast.LENGTH_LONG).show());
    }

    private void route(View view) {
        repository.loadTarget(target -> {
            if (target > 0) {
                viewModel.setDailyTarget(target);
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