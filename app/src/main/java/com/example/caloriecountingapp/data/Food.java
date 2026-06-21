package com.example.caloriecountingapp.data;

// One raw ingredient from foods.json. Nutrition values are per 100g.
public class Food {
    public String name;
    public String category;
    public int calories;
    public double protein;
    public double carbs;
    public double fat;

    // Calories for a given amount in grams
    public int caloriesForGrams(double grams) {
        return (int) Math.round(calories * grams / 100.0);
    }

    // Spinner shows this text
    @Override
    public String toString() {
        return name;
    }
}