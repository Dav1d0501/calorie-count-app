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
import com.example.caloriecountingapp.data.Meal;
import com.example.caloriecountingapp.databinding.FragmentSummaryBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

// Main screen: progress, macros, 7-day overview, and today's meal list
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

        // Load today's meals so the list and totals persist across restarts
        repository.loadTodayMeals(meals -> viewModel.setMeals(meals));

        repository.loadLastDaysTotal(7, (total, daysWithData) -> {
            int avg = daysWithData > 0 ? total / daysWithData : 0;
            binding.weekText.setText(getString(R.string.week_label, total, avg));
        });

        viewModel.getDailyTarget().observe(getViewLifecycleOwner(), t -> updateUI());
        viewModel.getMeals().observe(getViewLifecycleOwner(), m -> updateUI());

        binding.addFoodButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_summary_to_food));

        binding.updateGoalButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_summary_to_goal));

        binding.resetButton.setOnClickListener(v -> {
            viewModel.clearMeals();
            repository.saveMeals(viewModel.getMeals().getValue());
        });

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Navigation.findNavController(view).navigate(R.id.action_summary_to_login);
        });
    }

    private void updateUI() {
        int target = val(viewModel.getDailyTarget());
        int eaten = viewModel.getTotalCal();
        int remaining = target - eaten;

        binding.remainingText.setText(getString(R.string.remaining_label, remaining));
        binding.targetText.setText(getString(R.string.target_label, target));
        binding.eatenText.setText(getString(R.string.eaten_label, eaten));
        // Target macros from the calorie goal: 30% protein, 40% carbs, 30% fat
        int targetProtein = (int) Math.round(target * 0.30 / 4);
        int targetCarbs = (int) Math.round(target * 0.40 / 4);
        int targetFat = (int) Math.round(target * 0.30 / 9);
        binding.macrosText.setText(getString(R.string.macros_goal_label,
                viewModel.getTotalProtein(), targetProtein,
                viewModel.getTotalCarbs(), targetCarbs,
                viewModel.getTotalFat(), targetFat));
        // Progress bar: percent of target eaten (capped at 100)
        int percent = target > 0 ? Math.min(100, eaten * 100 / target) : 0;
        binding.progressBar.setProgress(percent);

        // Build the meal list text
        List<Meal> meals = viewModel.getMeals().getValue();
        if (meals == null || meals.isEmpty()) {
            binding.mealsText.setText(getString(R.string.no_meals));
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < meals.size(); i++) {
                Meal m = meals.get(i);
                sb.append(getString(R.string.meal_row, m.name, m.grams, m.cal));
                if (i < meals.size() - 1) sb.append("\n");
            }
            binding.mealsText.setText(sb.toString());
        }
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