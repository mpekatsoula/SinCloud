package erebus.sincloud.Activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import erebus.sincloud.R;

public class LoginActivity extends AppCompatActivity
{
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;

    // Constant for detecting the google login intent result
    private static final int GOOGLE_RC_SIGN_IN = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create();
        LoginButton facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email", "public_profile");

        // Callback registration
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(final LoginResult loginResult)
            {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel()
            {
            }

            @Override
            public void onError(FacebookException error)
            {
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        findViewById(R.id.google_login_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_RC_SIGN_IN);
            }
        });

        findViewById(R.id.activity_login_signup_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Open signup activity
                openSignupActivity();
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
        {
            openMainActivity(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // If the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == GOOGLE_RC_SIGN_IN)
        {
            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Authenticating with firebase
                assert account != null;
                handleGoogleAccessToken(account);
            }
            catch (ApiException e)
            {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        // Else the requested code comes from Facebook
        else
        {
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleGoogleAccessToken(GoogleSignInAccount acct)
    {
        // Get the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            openMainActivity(task.getResult().getAdditionalUserInfo().isNewUser());
                        }
                        else
                        {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getString(R.string.authentication_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(final AccessToken token)
    {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            openMainActivity(task.getResult().getAdditionalUserInfo().isNewUser());
                        }
                        else
                        {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getString(R.string.login_fb_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openMainActivity(boolean isNewUser)
    {
        // If it's the first time the user logs in,
        // create/update the firebase database with the username
        // and photoURL
        if(isNewUser)
        {
            FirebaseUser user = mAuth.getCurrentUser();
            if(user != null)
            {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                userRef.child("displayName").setValue(user.getDisplayName());
                if(user.getPhotoUrl() != null)
                {
                    userRef.child("photoURL").setValue(user.getPhotoUrl().toString());
                }
                else
                {
                    String[] images = {"https://firebasestorage.googleapis.com/v0/b/sincloud-3c2ea.appspot.com/o/app%2Favatar_bad_small.png?alt=media&token=350c7391-a97b-4a2b-a188-38a29417d8be",
                                       "https://firebasestorage.googleapis.com/v0/b/sincloud-3c2ea.appspot.com/o/app%2Favatar_good_small.png?alt=media&token=a30995bd-6b73-4cbb-8f5d-e3be5d4f7e4a"};
                    Random rand = new Random();
                    userRef.child("photoURL").setValue(images[rand.nextInt(1)]);
                }
            }
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPreferences.getBoolean(OnBoardingActivity.COMPLETED_ONBOARDING, false))
        {
            // This is the first time running the app, let's go to onboarding
            openOnBoardingActivity();
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void openOnBoardingActivity()
    {
        Intent intent = new Intent(getApplicationContext(), OnBoardingActivity.class);
        startActivity(intent);
        finish();
    }
    private void openSignupActivity()
    {
        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);
    }

}
