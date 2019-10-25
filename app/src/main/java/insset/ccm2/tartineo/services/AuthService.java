package insset.ccm2.tartineo.services;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {

    private static final AuthService instance = new AuthService();

    private FirebaseAuth firebaseAuth;

    private AuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static AuthService getInstance() {
        return instance;
    }

    /**
     * Retrieve the current User.
     *
     * @return FirebaseUser
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Register a new User by email and password.
     *
     * @param email The User email.
     * @param password The User password.
     *
     * @return Task with AuthResult response.
     */
    public Task<AuthResult> registerWithEmailAndPassword(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Login a User by email and password.
     *
     * @param email The User email.
     * @param password The User password.
     *
     * @return Task with AuthResult response.
     */
    public Task<AuthResult> loginWithEmailAndPassword(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Logout the current User.
     */
    public void logout() {
        firebaseAuth.signOut();
    }
}