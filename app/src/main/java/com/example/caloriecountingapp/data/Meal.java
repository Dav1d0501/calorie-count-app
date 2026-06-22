package com.example.caloriecountingapp.data;

// One logged food entry within a day
public class Meal {
    public String name;
    public int grams;
    public int cal;
    public int protein;
    public int carbs;
    public int fat;

    // Empty constructor required by Firestore
    public Meal() { }

    public Meal(String name, int grams, int cal, int protein, int carbs, int fat) {
        this.name = name;
        this.grams = grams;
        this.cal = cal;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
}