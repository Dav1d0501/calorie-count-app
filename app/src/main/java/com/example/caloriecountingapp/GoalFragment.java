package com.example.caloriecountingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.caloriecountingapp.data.FirestoreRepository;
import com.example.caloriecountingapp.databinding.FragmentGoalBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;

// Collects body data + target weight, computes the daily calorie target
public class GoalFragment extends Fragment {

    private FragmentGoalBinding binding;
    private UserViewModel viewModel;
    private final FirestoreRepository repository = new FirestoreRepository();
    private int dailyTarget = 0;

    private final String[] activityLevels = {
            "Sedentary", "Lightly active", "Active", "Very active"
    };
    // TDEE multipliers, same order as activityLevels
    private final double[] activityFactors = { 1.2, 1.375, 1.55, 1.725 };

    private final String[] rates = { "Slow", "Medium", "Fast" };
    // kg per week toward the target weight, same order as rates
    private final double[] rateKgPerWeek = { 0.25, 0.5, 0.75 };

    // 1 kg body weight is about 7700 kcal
    private static final double KCAL_PER_KG = 7700.0;

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
        binding.rateSpinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, rates));

        binding.calculateButton.setOnClickListener(v -> calculate());

        binding.continueButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_goal_to_summary));
    }

    private void calculate() {
        String ageStr = binding.ageInput.getText().toString();
        String heightStr = binding.heightInput.getText().toString();
        String weightStr = binding.weightInput.getText().toString();
        String targetWeightStr = binding.targetWeightInput.getText().toString();

        if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()
                || targetWeightStr.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        int height = Integer.parseInt(heightStr);
        double weight = Double.parseDouble(weightStr);
        double targetWeight = Double.parseDouble(targetWeightStr);
        boolean isMale = binding.sexMale.isChecked();

        // Mifflin-St Jeor BMR, then maintenance calories
        double bmr = (10 * weight) + (6.25 * height) - (5 * age) + (isMale ? 5 : -161);
        double maintenance = bmr * activityFactors[binding.activitySpinner.getSelectedItemPosition()];

        // Daily calorie change from the chosen weekly rate
        double kgPerWeek = rateKgPerWeek[binding.rateSpinner.getSelectedItemPosition()];
        double dailyChange = (kgPerWeek * KCAL_PER_KG) / 7.0;

        // Direction: below current weight = deficit, above = surplus, equal = maintain
        if (targetWeight < weight) {
            dailyTarget = (int) Math.round(maintenance - dailyChange);
        } else if (targetWeight > weight) {
            dailyTarget = (int) Math.round(maintenance + dailyChange);
        } else {
            dailyTarget = (int) Math.round(maintenance);
        }

        binding.resultText.setText(getString(R.string.daily_target, dailyTarget));
        viewModel.setDailyTarget(dailyTarget);
        repository.saveTarget(dailyTarget);
        binding.continueButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}