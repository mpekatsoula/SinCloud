package erebus.sincloud.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import erebus.sincloud.R;

public class SignupActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private TextView emailView;
    private TextView password;
    private TextView passwordRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailView = findViewById(R.id.email_signup_textview);
        password = findViewById(R.id.password_signup_textview);
        passwordRepeat = findViewById(R.id.password_signup_repeat);

        findViewById(R.id.signup_login_signup_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                RegisterUser();
            }
        });

        findViewById(R.id.signup_login_signup_button_back).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
    private void RegisterUser()
    {
        // Validate that user data are correct
        final String email = emailView.getText().toString();
        String pass = password.getText().toString();
        String pass_repeat = passwordRepeat.getText().toString();
        if(email.isEmpty())
        {
            Toast.makeText(SignupActivity.this, "Email cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!pass_repeat.equals(pass))
        {
            Toast.makeText(SignupActivity.this, "Passwords don't match.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass.isEmpty())
        {
            Toast.makeText(SignupActivity.this, "Passwords cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog signup_progress = ProgressDialog.show(this, "Signing up", "Connecting with the cloud!", true);
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful())
                        {
                            try
                            {
                                throw task.getException();
                            }
                            catch(FirebaseAuthWeakPasswordException e)
                            {
                                Toast.makeText(SignupActivity.this, "Weak Password.", Toast.LENGTH_SHORT).show();
                            }
                            catch(FirebaseAuthInvalidCredentialsException e)
                            {
                                Toast.makeText(SignupActivity.this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                            }
                            catch(FirebaseAuthUserCollisionException e)
                            {
                                Toast.makeText(SignupActivity.this, "User already exists.", Toast.LENGTH_SHORT).show();
                            }
                            catch(Exception e)
                            {
                                Toast.makeText(SignupActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                Crashlytics.logException(e);
                            }
                            Log.w(TAG, "signInWithEmail", task.getException());
                            signup_progress.dismiss();
                        }
                        else
                        {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(email)
                                    .build();

                            // Store photoURL uri to database
                            DatabaseReference users_ref = FirebaseDatabase.getInstance().getReference().child("users");
                            Map<String, String> map = new HashMap<>();
                            try
                            {
                                List<String> providers = user.getProviders();
                                if (providers != null)
                                {
                                    map.put("provider", providers.get(0));
                                }
                                else
                                {
                                    map.put("provider", "unknown");
                                }
                            }
                            catch (NullPointerException e)
                            {
                                Crashlytics.logException(e);
                                map.put("provider", "unknown");
                            }

                            map.put("nickname", "Anonymous");

                            int index = email.indexOf('@');
                            map.put("displayName",email.substring(0,index));
                            users_ref.child(user.getUid()).setValue(map);

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            signup_progress.dismiss();
                                            if (task.isSuccessful())
                                            {
                                                Log.d(TAG, "User profile updated.");
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(SignupActivity.this, "Something went wrong, please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
        }

}
