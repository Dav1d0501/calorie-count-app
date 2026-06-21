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

import com.example.caloriecountingapp.data.Food;
import com.example.caloriecountingapp.data.FirestoreRepository;
import com.example.caloriecountingapp.databinding.FragmentFoodBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;
import com.google.gson.Gson;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Loads the ingredient catalog, computes calories + macros for an amount, logs it
public class FoodFragment extends Fragment {

    private FragmentFoodBinding binding;
    private UserViewModel viewModel;
    private final FirestoreRepository repository = new FirestoreRepository();
    private List<Food> foods;

    // Last calculated values, committed when the user taps Add
    private int lastCal = 0, lastProtein = 0, lastCarbs = 0, lastFat = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFoodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        foods = loadFoods();

        ArrayAdapter<Food> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, foods);
        binding.foodSpinner.setAdapter(adapter);

        binding.backButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        binding.calculateButton.setOnClickListener(v -> calculate());

        binding.addButton.setOnClickListener(v -> {
            viewModel.addFood(lastCal, lastProtein, lastCarbs, lastFat);
            repository.saveToday(
                    viewModel.getEaten().getValue(),
                    viewModel.getProtein().getValue(),
                    viewModel.getCarbs().getValue(),
                    viewModel.getFat().getValue());
            Toast.makeText(getContext(), getString(R.string.added), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
        });
    }

    private List<Food> loadFoods() {
        try {
            InputStream is = getResources().openRawResource(R.raw.foods);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            FoodWrapper wrapper = new Gson().fromJson(json, FoodWrapper.class);
            return wrapper.foods;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void calculate() {
        String gramsStr = binding.gramsInput.getText().toString();
        String typedName = binding.foodSpinner.getText().toString().trim();

        if (typedName.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.pick_ingredient), Toast.LENGTH_SHORT).show();
            return;
        }
        if (gramsStr.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.enter_grams), Toast.LENGTH_SHORT).show();
            return;
        }

        // Match the typed text to a food in the catalog
        Food selected = null;
        for (Food f : foods) {
            if (f.name.equalsIgnoreCase(typedName)) {
                selected = f;
                break;
            }
        }
        if (selected == null) {
            Toast.makeText(getContext(), getString(R.string.ingredient_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        double grams = Double.parseDouble(gramsStr);
        lastCal = selected.caloriesForGrams(grams);
        lastProtein = selected.proteinForGrams(grams);
        lastCarbs = selected.carbsForGrams(grams);
        lastFat = selected.fatForGrams(grams);

        binding.caloriesText.setText(getString(R.string.food_result,
                selected.name, (int) grams, lastCal, lastProtein, lastCarbs, lastFat));
        binding.addButton.setVisibility(View.VISIBLE);
    }

    // Top-level shape of foods.json
    private static class FoodWrapper {
        List<Food> foods;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}