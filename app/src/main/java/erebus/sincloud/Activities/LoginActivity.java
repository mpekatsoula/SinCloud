package erebus.sincloud.Activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
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

import erebus.sincloud.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;

    // Constant for detecting the google login intent result
    private static final int GOOGLE_RC_SIGN_IN = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Facebook Login button
        callbackManager = CallbackManager.Factory.create();
        LoginButton facebookLoginButton = findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions("email", "public_profile");

        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                handleAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("942729372464-rr96u77kot5c3ek2rlemt8l7p8burisd.apps.googleusercontent.com")
                .requestEmail()
                .build();

        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        findViewById(R.id.google_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            openMainActivity();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the requestCode is the Google Sign In code that we defined at starting
        // means that user successfully signed in to his google account
        if (requestCode == GOOGLE_RC_SIGN_IN) {
            //Getting the GoogleSignIn Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Authenticating with firebase
                assert account != null;
                handleAccessToken(account);
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        // Else the requested code comes from Facebook
        else {
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Registers user in our firebase
     *
     * @param acct google/facebook access token
     */
    private void handleAccessToken(Object acct) {
        AuthCredential credential;
        String _api;

        // Token is from Google account
        if (acct instanceof GoogleSignInAccount) {

            GoogleSignInAccount _acct = (GoogleSignInAccount) acct;
            _api = "Google";
            Log.d(TAG, "handle" + _api + "AccessToken:" + _acct.getId());
            credential = GoogleAuthProvider.getCredential(_acct.getIdToken(), null);

        }
        // Token is from Facebook account
        else {

            AccessToken _acct = (AccessToken) acct;
            _api = "Facebook";
            Log.d(TAG, "handle" + _api + "AccessToken:" + _acct.getUserId());
            credential = FacebookAuthProvider.getCredential(_acct.getToken());

        }

        final String api = _api;

        // Now using firebase we are signing in the user here
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "handle" + api + "AccessToken:success");
                            openMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "handle" + api + "AccessToken:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openMainActivity() {
        // [BUG FIX] MainActivity Intent should clear top so user can exit the app when presses back
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
