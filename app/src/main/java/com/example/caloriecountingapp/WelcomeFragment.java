package com.example.caloriecountingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.caloriecountingapp.databinding.FragmentWelcomeBinding;

// מסך הפתיחה של האפליקציה. מציג את המיתוג ושני כפתורי כניסה.
public class WelcomeFragment extends Fragment {

    // אובייקט ה-ViewBinding. מאפשר גישה בטוחה לרכיבי ה-layout בלי findViewById.
    private FragmentWelcomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // "מנפחים" את ה-layout ומקבלים גישה לרכיבים דרך binding
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // לחיצה על "Get started" - בהמשך תנווט למסך ההרשמה
        binding.btnGetStarted.setOnClickListener(v ->
                Toast.makeText(getContext(), "Get started", Toast.LENGTH_SHORT).show());

        // לחיצה על "I already have an account" - בהמשך תנווט למסך התחברות
        binding.btnHaveAccount.setOnClickListener(v ->
                Toast.makeText(getContext(), "Sign in", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // מנקים את ה-binding כדי למנוע דליפת זיכרון כשה-Fragment נהרס
        binding = null;
    }
}