package erebus.sincloud.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import erebus.sincloud.R;

public class SignupActivity extends AppCompatActivity
{
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
            Toast.makeText(SignupActivity.this, getString(R.string.singup_email_empty_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if(!pass_repeat.equals(pass))
        {
            Toast.makeText(SignupActivity.this, getString(R.string.signup_passwords_match_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if(pass.isEmpty())
        {
            Toast.makeText(SignupActivity.this, getString(R.string.signup_password_empty_error), Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog signup_progress = ProgressDialog.show(this, getString(R.string.singup_progress_dialog_title), getString(R.string.singup_progress_dialog_text), true);
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
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
                                Toast.makeText(SignupActivity.this, getString(R.string.singup_weak_password), Toast.LENGTH_SHORT).show();
                            }
                            catch(FirebaseAuthInvalidCredentialsException e)
                            {
                                Toast.makeText(SignupActivity.this, getString(R.string.signup_invalid_credentials), Toast.LENGTH_SHORT).show();
                            }
                            catch(FirebaseAuthUserCollisionException e)
                            {
                                Toast.makeText(SignupActivity.this, getString(R.string.singup_user_already_exists), Toast.LENGTH_SHORT).show();
                            }
                            catch(Exception e)
                            {
                                Toast.makeText(SignupActivity.this, getString(R.string.singup_generic_error), Toast.LENGTH_SHORT).show();
                                Crashlytics.logException(e);
                            }
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
                                List<? extends UserInfo> providers = Objects.requireNonNull(user).getProviderData();
                                map.put("provider", providers.get(0).getProviderId());
                            }
                            catch (NullPointerException e)
                            {
                                Crashlytics.logException(e);
                                map.put("provider", "unknown");
                            }

                            map.put("nickname", "Anonymous");

                            int index = email.indexOf('@');
                            map.put("displayName",email.substring(0,index));

                            String[] images = {"https://firebasestorage.googleapis.com/v0/b/sincloud-3c2ea.appspot.com/o/app%2Favatar_bad_small.png?alt=media&token=350c7391-a97b-4a2b-a188-38a29417d8be",
                                    "https://firebasestorage.googleapis.com/v0/b/sincloud-3c2ea.appspot.com/o/app%2Favatar_good_small.png?alt=media&token=a30995bd-6b73-4cbb-8f5d-e3be5d4f7e4a"};
                            Random rand = new Random();
                            map.put("photoURL", images[rand.nextInt(1)]);

                            users_ref.child(Objects.requireNonNull(user).getUid()).setValue(map);
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            signup_progress.dismiss();
                                            if (task.isSuccessful())
                                            {
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    Toast.makeText(SignupActivity.this, R.string.singup_generic_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
        }

}
