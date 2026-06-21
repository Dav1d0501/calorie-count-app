package com.example.caloriecountingapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Shared state across fragments: daily target and today's running totals
public class UserViewModel extends ViewModel {

    private final MutableLiveData<Integer> dailyTarget = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> eaten = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> protein = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> carbs = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> fat = new MutableLiveData<>(0);

    public MutableLiveData<Integer> getDailyTarget() { return dailyTarget; }
    public void setDailyTarget(int target) { dailyTarget.setValue(target); }

    public MutableLiveData<Integer> getEaten() { return eaten; }
    public MutableLiveData<Integer> getProtein() { return protein; }
    public MutableLiveData<Integer> getCarbs() { return carbs; }
    public MutableLiveData<Integer> getFat() { return fat; }

    // Add one logged food to today's totals
    public void addFood(int cal, int p, int c, int f) {
        eaten.setValue(safe(eaten) + cal);
        protein.setValue(safe(protein) + p);
        carbs.setValue(safe(carbs) + c);
        fat.setValue(safe(fat) + f);
    }

    // Replace totals, used when loading a day from Firestore
    public void setTotals(int cal, int p, int c, int f) {
        eaten.setValue(cal);
        protein.setValue(p);
        carbs.setValue(c);
        fat.setValue(f);
    }

    private int safe(MutableLiveData<Integer> v) {
        return v.getValue() != null ? v.getValue() : 0;
    }
}