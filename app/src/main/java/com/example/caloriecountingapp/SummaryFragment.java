package com.example.caloriecountingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.caloriecountingapp.databinding.FragmentSummaryBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;

// Summary screen: shows target vs. eaten vs. remaining, reading from the shared ViewModel.
public class SummaryFragment extends Fragment {

    private FragmentSummaryBinding binding;
    private UserViewModel viewModel;

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

        // Re-draw whenever the logged calories change
        viewModel.getLoggedCalories().observe(getViewLifecycleOwner(), list -> updateUI());
        viewModel.getDailyTarget().observe(getViewLifecycleOwner(), target -> updateUI());

        binding.addFoodButton.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_summary_to_food));
    }

    // Pulls current values from the ViewModel and updates the three text views
    private void updateUI() {
        int target = viewModel.getDailyTarget().getValue() != null
                ? viewModel.getDailyTarget().getValue() : 0;
        int eaten = viewModel.getTotalEaten();
        int remaining = target - eaten;

        binding.targetText.setText("Target: " + target + " kcal");
        binding.eatenText.setText("Eaten: " + eaten + " kcal");
        binding.remainingText.setText("Remaining: " + remaining + " kcal");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}