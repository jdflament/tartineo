package insset.ccm2.tartineo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activité contenant l'ensemble de la logique liée
 * à l'inscription de l'utilisateur sur l'application.
 */
public class RegisterActivity extends AppCompatActivity {

    private final static String REGISTER_TAG = "REGISTER_ACTIVITY";

    EditText nameText, emailText, passwordText, passwordConfirmationText;

    Button registerButton, backButton;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.i(REGISTER_TAG, "RegisterActivity initialization.");

        // Fields
        nameText = findViewById(R.id.register_name_text);
        emailText = findViewById(R.id.register_email_text);
        passwordText = findViewById(R.id.register_password_text);
        passwordConfirmationText = findViewById(R.id.register_password_confirmation_text);

        // Buttons
        registerButton = findViewById(R.id.register_submit_button);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Register a new user in the application.
     * Fired on register button's click.
     *
     * @param view RegisterActivity view.
     */
    public void register(View view) {
        Log.i(REGISTER_TAG, getStringRes(R.string.info_register_button_fired));

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirmation = passwordConfirmationText.getText().toString();

        if (TextUtils.isEmpty(name)|| TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_empty_field));
            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_empty_field), Toast.LENGTH_SHORT).show();
        }

        if (password.length() < 6) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_password_length));
            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_password_length), Toast.LENGTH_SHORT).show();
        }

        if (!TextUtils.equals(password, passwordConfirmation)) {
            Log.e(REGISTER_TAG, getStringRes(R.string.error_different_passwords));
            Toast.makeText(getApplicationContext(), getStringRes(R.string.error_different_passwords), Toast.LENGTH_SHORT).show();
        }

        /*
         * Création d'un Utilisateur Firebase.
         */
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Redirection sur l'activité principale et enregistrement en base en cas de succès.
                if (task.isSuccessful()) {
                    Log.i(REGISTER_TAG, getStringRes(R.string.info_registration_successful));

                    // TODO : Create CardActivity, then redirect to this one.

                    Toast.makeText(getApplicationContext(), "Registration successful.", Toast.LENGTH_SHORT).show();
                    // startActivity(new Intent(getApplicationContext(), CardActivity.class));

                    finish();
                }
                else {
                    Log.w(REGISTER_TAG, getStringRes(R.string.error_incorrect_email_or_password), task.getException());

                    Toast.makeText(getApplicationContext(), getStringRes(R.string.error_incorrect_email_or_password), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     *
     * @param id Id of the string resource.
     *
     * @return String resource.
     */
    private String getStringRes(int id) {
        return getResources().getString(id);
    }
}
