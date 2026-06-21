package com.example.caloriecountingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.caloriecountingapp.data.FirestoreRepository;
import com.example.caloriecountingapp.databinding.FragmentSummaryBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

// Shows today's totals, macros, and a 7-day overview
public class SummaryFragment extends Fragment {

    private FragmentSummaryBinding binding;
    private UserViewModel viewModel;
    private final FirestoreRepository repository = new FirestoreRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Load today's saved totals so the count continues across restarts
        repository.loadDay(0, (eaten, protein, carbs, fat) ->
                viewModel.setTotals(eaten, protein, carbs, fat));

        repository.loadLastDaysTotal(7, (total, daysWithData) -> {
            int avg = daysWithData > 0 ? total / daysWithData : 0;
            binding.weekText.setText(getString(R.string.week_label, total, avg));
        });

        viewModel.getDailyTarget().observe(getViewLifecycleOwner(), t -> updateUI());
        viewModel.getEaten().observe(getViewLifecycleOwner(), e -> updateUI());
        viewModel.getProtein().observe(getViewLifecycleOwner(), p -> updateUI());

        binding.addFoodButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_summary_to_food));

        binding.updateGoalButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_summary_to_goal));

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(view).navigate(R.id.action_summary_to_login);
        });
    }

    private void updateUI() {
        int target = val(viewModel.getDailyTarget());
        int eaten = val(viewModel.getEaten());
        int remaining = target - eaten;

        binding.targetText.setText(getString(R.string.target_label, target));
        binding.eatenText.setText(getString(R.string.eaten_label, eaten));
        binding.remainingText.setText(getString(R.string.remaining_label, remaining));
        binding.macrosText.setText(getString(R.string.macros_label,
                val(viewModel.getProtein()), val(viewModel.getCarbs()), val(viewModel.getFat())));
    }

    private int val(androidx.lifecycle.LiveData<Integer> v) {
        return v.getValue() != null ? v.getValue() : 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}