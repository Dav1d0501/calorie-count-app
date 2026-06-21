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
import com.example.caloriecountingapp.databinding.FragmentFoodBinding;
import com.example.caloriecountingapp.viewmodel.UserViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Food screen: loads the foods catalog, computes calories for an amount, logs it.
public class FoodFragment extends Fragment {

    private FragmentFoodBinding binding;
    private UserViewModel viewModel;
    private final com.example.caloriecountingapp.data.FirestoreRepository repository =
            new com.example.caloriecountingapp.data.FirestoreRepository();
    private List<Food> foods;
    private int lastCalculatedCalories = 0;

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

        // Searchable dropdown: filters the 253 ingredients as the user types
        ArrayAdapter<Food> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, foods);
        binding.foodSpinner.setAdapter(adapter);

        binding.calculateButton.setOnClickListener(v -> calculate());

        binding.addButton.setOnClickListener(v -> {
            viewModel.addCalories(lastCalculatedCalories);
            repository.saveTodayEaten(viewModel.getTotalEaten());  // persist today's total
            Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show();
            // Go back to Summary, which updates automatically
            Navigation.findNavController(view).navigateUp();
        });
    }

    // Reads res/raw/foods.json into a list of Food objects
    private List<Food> loadFoods() {
        InputStream is = getResources().openRawResource(R.raw.foods);
        byte[] buffer;
        try {
            buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
        String json = new String(buffer, StandardCharsets.UTF_8);

        // foods.json is { "foods": [ ... ] }, so parse the wrapper then take the list
        Gson gson = new Gson();
        FoodWrapper wrapper = gson.fromJson(json, FoodWrapper.class);
        return wrapper.foods;
    }

    private void calculate() {
        String gramsStr = binding.gramsInput.getText().toString();
        String typedName = binding.foodSpinner.getText().toString().trim();

        if (typedName.isEmpty()) {
            Toast.makeText(getContext(), "Pick an ingredient", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gramsStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter grams", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the matching food by name (case-insensitive)
        Food selected = null;
        for (Food f : foods) {
            if (f.name.equalsIgnoreCase(typedName)) {
                selected = f;
                break;
            }
        }
        if (selected == null) {
            Toast.makeText(getContext(), "Ingredient not found", Toast.LENGTH_SHORT).show();
            return;
        }

        double grams = Double.parseDouble(gramsStr);
        lastCalculatedCalories = selected.caloriesForGrams(grams);

        binding.caloriesText.setText(
                selected.name + " (" + (int) grams + "g) = " + lastCalculatedCalories + " kcal");
        binding.addButton.setVisibility(View.VISIBLE);
    }

    // Matches the top-level shape of foods.json
    private static class FoodWrapper {
        List<Food> foods;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}