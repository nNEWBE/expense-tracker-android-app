package com.example.expensetrackerapp.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.expensetrackerapp.MainActivity;
import com.example.expensetrackerapp.auth.AuthManager;
import com.example.expensetrackerapp.databinding.ActivitySplashBinding;
import com.example.expensetrackerapp.utils.PreferenceManager;

/**
 * Modern Splash Screen with Logo Animation.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private AuthManager authManager;
    private PreferenceManager preferenceManager;

    private static final int SPLASH_DURATION = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle system splash screen API 12+
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Make fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance();
        preferenceManager = PreferenceManager.getInstance(this);

        // Apply saved theme
        preferenceManager.applyTheme(preferenceManager.getThemeMode());

        // Start animations
        startAnimations();

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    private void startAnimations() {
        // Logo Animation: Scale Up and Rotate
        binding.ivLogo.setScaleX(0f);
        binding.ivLogo.setScaleY(0f);
        binding.ivLogo.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.ivLogo, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.ivLogo, "scaleY", 0f, 1f);
        ObjectAnimator alphaLogo = ObjectAnimator.ofFloat(binding.ivLogo, "alpha", 0f, 1f);

        scaleX.setDuration(1200);
        scaleY.setDuration(1200);
        alphaLogo.setDuration(800);

        scaleX.setInterpolator(new OvershootInterpolator(1.2f));
        scaleY.setInterpolator(new OvershootInterpolator(1.2f));

        // Text Animation: Slide Up and Fade In
        binding.tvAppName.setTranslationY(100f);
        binding.tvAppName.setAlpha(0f);

        ObjectAnimator slideUpName = ObjectAnimator.ofFloat(binding.tvAppName, "translationY", 100f, 0f);
        ObjectAnimator fadeName = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f);
        slideUpName.setDuration(1000);
        fadeName.setDuration(1000);
        slideUpName.setInterpolator(new AccelerateDecelerateInterpolator());

        // Tagline Animation
        binding.tvTagline.setTranslationY(50f);
        binding.tvTagline.setAlpha(0f);

        ObjectAnimator slideUpTag = ObjectAnimator.ofFloat(binding.tvTagline, "translationY", 50f, 0f);
        ObjectAnimator fadeTag = ObjectAnimator.ofFloat(binding.tvTagline, "alpha", 0f, 1f);
        slideUpTag.setDuration(1000);
        fadeTag.setDuration(1000);

        // Choreograph animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleX).with(scaleY).with(alphaLogo);
        animatorSet.play(slideUpName).with(fadeName).after(400);
        animatorSet.play(slideUpTag).with(fadeTag).after(800);

        animatorSet.start();
    }

    private void navigateToNextScreen() {
        Intent intent;

        if (authManager.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
