package com.example.caloriecountingapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.caloriecountingapp.data.Meal;

import java.util.ArrayList;
import java.util.List;

// Shared state: daily target and today's logged meals
public class UserViewModel extends ViewModel {

    private final MutableLiveData<Integer> dailyTarget = new MutableLiveData<>(0);
    private final MutableLiveData<List<Meal>> meals = new MutableLiveData<>(new ArrayList<>());

    public MutableLiveData<Integer> getDailyTarget() { return dailyTarget; }
    public void setDailyTarget(int target) { dailyTarget.setValue(target); }

    public MutableLiveData<List<Meal>> getMeals() { return meals; }

    // Add one meal to today's list
    public void addMeal(Meal meal) {
        List<Meal> current = meals.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(meal);
        meals.setValue(current);
    }

    // Replace the whole list, used when loading from Firestore
    public void setMeals(List<Meal> list) {
        meals.setValue(list != null ? list : new ArrayList<>());
    }

    // Clear today's meals
    public void clearMeals() {
        meals.setValue(new ArrayList<>());
    }

    // Computed totals
    public int getTotalCal() { return sum(0); }
    public int getTotalProtein() { return sum(1); }
    public int getTotalCarbs() { return sum(2); }
    public int getTotalFat() { return sum(3); }

    private int sum(int which) {
        int total = 0;
        List<Meal> current = meals.getValue();
        if (current != null) {
            for (Meal m : current) {
                switch (which) {
                    case 0: total += m.cal; break;
                    case 1: total += m.protein; break;
                    case 2: total += m.carbs; break;
                    case 3: total += m.fat; break;
                }
            }
        }
        return total;
    }
}