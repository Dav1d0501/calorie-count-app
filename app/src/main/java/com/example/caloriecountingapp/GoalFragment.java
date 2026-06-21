package com.example.caloriecountingapp;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.example.caloriecountingapp.viewmodel.UserViewModel;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.caloriecountingapp.databinding.FragmentGoalBinding;

public class GoalFragment extends Fragment {

    private FragmentGoalBinding binding;

    private final String[] activityLevels = {
            "Sedentary", "Lightly active", "Active", "Very active"
    };
    private final double[] activityFactors = { 1.2, 1.375, 1.55, 1.725 };

    private final String[] goals = { "Lose weight", "Maintain", "Build muscle" };
    private final int[] goalAdjustments = { -500, 0, 300 };

    private int dailyTarget = 0;
    private UserViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGoalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        binding.activitySpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, activityLevels));
        binding.goalSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, goals));

        binding.calculateButton.setOnClickListener(v -> calculate());

        binding.continueButton.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_goal_to_summary));
    }

    private void calculate() {
        String ageStr = binding.ageInput.getText().toString();
        String heightStr = binding.heightInput.getText().toString();
        String weightStr = binding.weightInput.getText().toString();

        if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        int height = Integer.parseInt(heightStr);
        double weight = Double.parseDouble(weightStr);
        boolean isMale = binding.sexMale.isChecked();

        double bmr = (10 * weight) + (6.25 * height) - (5 * age) + (isMale ? 5 : -161);

        double factor = activityFactors[binding.activitySpinner.getSelectedItemPosition()];
        int adjustment = goalAdjustments[binding.goalSpinner.getSelectedItemPosition()];

        dailyTarget = (int) Math.round(bmr * factor) + adjustment;

        binding.resultText.setText("Your daily target: " + dailyTarget + " kcal");
        viewModel.setDailyTarget(dailyTarget);
        binding.continueButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}