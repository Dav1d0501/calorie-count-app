package com.example.caloriecountingapp.data;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Single place for all Firestore reads/writes, scoped to the logged-in user.
public class FirestoreRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // The current user's id; all data lives under users/{uid}/...
    private String uid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // Today's date as a document key, e.g. "2026-06-21"
    public static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
    }

    // Date key for N days ago (0 = today, 1 = yesterday, ...)
    public static String dayKey(int daysAgo) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    // Save the daily calorie target under the user's profile
    public void saveTarget(int target) {
        if (uid() == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("dailyTarget", target);
        db.collection("users").document(uid())
                .collection("profile").document("data")
                .set(data);
    }
    // Callback so the caller knows the result once Firestore responds
    public interface TargetCallback {
        void onResult(int target); // target = 0 means "no saved target"
    }

    // Read the saved daily target, if any
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
    // Save today's total eaten calories under users/{uid}/days/{today}
    public void saveTodayEaten(int totalEaten) {
        if (uid() == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("eaten", totalEaten);
        db.collection("users").document(uid())
                .collection("days").document(today())
                .set(data);
    }
}