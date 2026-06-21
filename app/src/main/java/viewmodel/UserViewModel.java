package com.example.caloriecountingapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

// Shared data across fragments: the daily calorie target and today's logged foods.
public class UserViewModel extends ViewModel {

    // The calculated daily calorie goal
    private final MutableLiveData<Integer> dailyTarget = new MutableLiveData<>(0);

    // List of calories logged today (each entry = one food added)
    private final MutableLiveData<List<Integer>> loggedCalories =
            new MutableLiveData<>(new ArrayList<>());

    public MutableLiveData<Integer> getDailyTarget() {
        return dailyTarget;
    }

    public void setDailyTarget(int target) {
        dailyTarget.setValue(target);
    }

    public MutableLiveData<List<Integer>> getLoggedCalories() {
        return loggedCalories;
    }

    // Add one food's calories to today's log
    public void addCalories(int calories) {
        List<Integer> current = loggedCalories.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(calories);
        loggedCalories.setValue(current);
    }

    // Sum of everything eaten today
    public int getTotalEaten() {
        int total = 0;
        List<Integer> current = loggedCalories.getValue();
        if (current != null) {
            for (int c : current) total += c;
        }
        return total;
    }
}