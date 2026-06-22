package com.example.caloriecountingapp.data;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// All Firestore reads/writes, scoped to the logged-in user
public class FirestoreRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private String uid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public static String today() {
        return dayKey(0);
    }

    // Date key N days back, e.g. "2026-06-22"
    public static String dayKey(int daysAgo) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    //  Target

    public void saveTarget(int target) {
        if (uid() == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("dailyTarget", target);
        db.collection("users").document(uid())
                .collection("profile").document("data")
                .set(data);
    }

    public interface TargetCallback { void onResult(int target); }

    public void loadTarget(@NonNull TargetCallback callback) {
        if (uid() == null) { callback.onResult(0); return; }
        db.collection("users").document(uid())
                .collection("profile").document("data")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getLong("dailyTarget") != null) {
                        callback.onResult(doc.getLong("dailyTarget").intValue());
                    } else {
                        callback.onResult(0);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(0));
    }

    //  Daily meals

    // Save the full meals list for today
    public void saveMeals(List<Meal> meals) {
        if (uid() == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("meals", meals);
        db.collection("users").document(uid())
                .collection("days").document(today())
                .set(data);
    }

    public interface MealsCallback { void onResult(List<Meal> meals); }

    // Load today's meals list
    public void loadTodayMeals(@NonNull MealsCallback callback) {
        if (uid() == null) { callback.onResult(new ArrayList<>()); return; }
        db.collection("users").document(uid())
                .collection("days").document(today())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Meal> meals = doc.toObject(DayDoc.class) != null
                                ? doc.toObject(DayDoc.class).meals : null;
                        callback.onResult(meals != null ? meals : new ArrayList<>());
                    } else {
                        callback.onResult(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    // Sum of calories across the last numDays days
    public interface WeekCallback { void onResult(int total, int daysWithData); }

    public void loadLastDaysTotal(int numDays, @NonNull WeekCallback callback) {
        if (uid() == null) { callback.onResult(0, 0); return; }
        final int[] total = {0};
        final int[] withData = {0};
        final int[] remaining = {numDays};

        for (int i = 0; i < numDays; i++) {
            db.collection("users").document(uid())
                    .collection("days").document(dayKey(i))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null
                                && task.getResult().exists()) {
                            DayDoc d = task.getResult().toObject(DayDoc.class);
                            if (d != null && d.meals != null && !d.meals.isEmpty()) {
                                int dayCal = 0;
                                for (Meal m : d.meals) dayCal += m.cal;
                                total[0] += dayCal;
                                withData[0]++;
                            }
                        }
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            callback.onResult(total[0], withData[0]);
                        }
                    });
        }
    }

    // Maps the day document shape for Firestore deserialization
    public static class DayDoc {
        public List<Meal> meals;
        public DayDoc() { }
    }
}