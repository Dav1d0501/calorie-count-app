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
import com.example.caloriecountingapp.data.Meal;
import com.example.caloriecountingapp.databinding.FragmentFoodBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;
import com.google.gson.Gson;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Loads the catalog, computes a meal for an amount, logs it
public class FoodFragment extends Fragment {

    private FragmentFoodBinding binding;
    private UserViewModel viewModel;
    private final FirestoreRepository repository = new FirestoreRepository();
    private List<Food> foods;

    // The meal built by the last Calculate, committed on Add
    private Meal pendingMeal;

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
            if (pendingMeal == null) return;
            viewModel.addMeal(pendingMeal);
            repository.saveMeals(viewModel.getMeals().getValue());
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

        int grams = (int) Double.parseDouble(gramsStr);
        pendingMeal = new Meal(
                selected.name,
                grams,
                selected.caloriesForGrams(grams),
                selected.proteinForGrams(grams),
                selected.carbsForGrams(grams),
                selected.fatForGrams(grams));

        binding.caloriesText.setText(getString(R.string.food_result,
                pendingMeal.name, pendingMeal.grams, pendingMeal.cal,
                pendingMeal.protein, pendingMeal.carbs, pendingMeal.fat));
        binding.addButton.setVisibility(View.VISIBLE);
    }

    private static class FoodWrapper {
        List<Food> foods;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}