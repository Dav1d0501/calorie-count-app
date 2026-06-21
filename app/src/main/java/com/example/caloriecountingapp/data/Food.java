package com.example.caloriecountingapp.data;

// One raw ingredient from foods.json. Values are per 100g.
public class Food {
    public String name;
    public String category;
    public int calories;
    public double protein;
    public double carbs;
    public double fat;

    public int caloriesForGrams(double grams) {
        return (int) Math.round(calories * grams / 100.0);
    }

    public int proteinForGrams(double grams) {
        return (int) Math.round(protein * grams / 100.0);
    }

    public int carbsForGrams(double grams) {
        return (int) Math.round(carbs * grams / 100.0);
    }

    public int fatForGrams(double grams) {
        return (int) Math.round(fat * grams / 100.0);
    }

    // Used as the label in the ingredient dropdown
    @Override
    public String toString() {
        return name;
    }
}