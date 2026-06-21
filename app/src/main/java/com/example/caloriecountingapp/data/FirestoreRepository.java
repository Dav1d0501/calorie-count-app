package com.example.caloriecountingapp.data;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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

    // Date key N days back, e.g. "2026-06-21"
    public static String dayKey(int daysAgo) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -daysAgo);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

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

    public void saveToday(int eaten, int protein, int carbs, int fat) {
        if (uid() == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("eaten", eaten);
        data.put("protein", protein);
        data.put("carbs", carbs);
        data.put("fat", fat);
        db.collection("users").document(uid())
                .collection("days").document(today())
                .set(data);
    }

    public interface DayCallback { void onResult(int eaten, int protein, int carbs, int fat); }

    public void loadDay(int daysAgo, @NonNull DayCallback callback) {
        if (uid() == null) { callback.onResult(0, 0, 0, 0); return; }
        db.collection("users").document(uid())
                .collection("days").document(dayKey(daysAgo))
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onResult(
                                intOf(doc.getLong("eaten")),
                                intOf(doc.getLong("protein")),
                                intOf(doc.getLong("carbs")),
                                intOf(doc.getLong("fat")));
                    } else {
                        callback.onResult(0, 0, 0, 0);
                    }
                })
                .addOnFailureListener(e -> callback.onResult(0, 0, 0, 0));
    }

    public interface WeekCallback { void onResult(int total, int daysWithData); }

    // Sums "eaten" over the last numDays days; fires callback once all reads return
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
                                && task.getResult().exists()
                                && task.getResult().getLong("eaten") != null) {
                            total[0] += task.getResult().getLong("eaten").intValue();
                            withData[0]++;
                        }
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            callback.onResult(total[0], withData[0]);
                        }
                    });
        }
    }

    private int intOf(Long v) { return v != null ? v.intValue() : 0; }
}