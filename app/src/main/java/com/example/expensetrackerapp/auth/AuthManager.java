package com.example.expensetrackerapp.auth;

import android.content.Context;

import com.example.expensetrackerapp.data.local.AppDatabase;
import com.example.expensetrackerapp.data.local.entity.UserProfile;
import com.example.expensetrackerapp.utils.Constants;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Manages Firebase Authentication and user state.
 * Singleton pattern for global access.
 */
public class AuthManager {

    private static AuthManager instance;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private FirebaseAuth.AuthStateListener authStateListener;

    private AuthManager() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    /**
     * Get current Firebase user.
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Check if user is logged in.
     */
    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Check if user is a guest.
     */
    public boolean isGuest() {
        return firebaseAuth.getCurrentUser() == null;
    }

    /**
     * Get current user ID (Firebase UID or "guest").
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : Constants.USER_GUEST;
    }

    /**
     * Get current user email.
     */
    public String getCurrentUserEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Sign up with email and password.
     */
    public Task<AuthResult> signUp(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Sign in with email and password.
     */
    public Task<AuthResult> signIn(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Send password reset email.
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    /**
     * Sign out current user.
     */
    public void signOut() {
        firebaseAuth.signOut();
    }

    /**
     * Delete current user account.
     */
    public Task<Void> deleteAccount() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            return user.delete();
        }
        return Tasks.forException(new Exception("No user logged in"));
    }

    /**
     * Set auth state listener.
     */
    public void setAuthStateListener(FirebaseAuth.AuthStateListener listener) {
        this.authStateListener = listener;
        firebaseAuth.addAuthStateListener(listener);
    }

    /**
     * Remove auth state listener.
     */
    public void removeAuthStateListener() {
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    /**
     * Create user profile in Firestore after registration.
     */
    public void createUserProfile(String userId, String email, String name, OnProfileCreatedListener listener) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setEmail(email);
        profile.setName(name);
        profile.setGuest(false);
        profile.setCurrency(Constants.CURRENCY_BDT);
        profile.setMonthlyBudget(0);

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection(Constants.COLLECTION_PROFILE)
                .document("info")
                .set(profile)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    /**
     * Create local guest profile in Room database.
     */
    public void createGuestProfile(Context context) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            UserProfile existingProfile = db.userProfileDao().getProfileSync(Constants.USER_GUEST);

            if (existingProfile == null) {
                UserProfile guestProfile = new UserProfile();
                guestProfile.setUserId(Constants.USER_GUEST);
                guestProfile.setName("Guest User");
                guestProfile.setGuest(true);
                guestProfile.setCurrency(Constants.CURRENCY_BDT);
                guestProfile.setMonthlyBudget(0);

                db.userProfileDao().insert(guestProfile);
            }
        });
    }

    /**
     * Callback interface for profile creation.
     */
    public interface OnProfileCreatedListener {
        void onSuccess();

        void onFailure(String error);
    }
}
