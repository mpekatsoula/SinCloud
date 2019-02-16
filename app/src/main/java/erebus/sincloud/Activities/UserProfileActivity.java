package erebus.sincloud.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;
import erebus.sincloud.Adapters.FragmentAdapter;
import erebus.sincloud.Fragments.HistoryFragment;
import erebus.sincloud.Fragments.SinsViewFragment;
import erebus.sincloud.R;
import erebus.sincloud.Singletons.SinAudioPlayer;
import erebus.sincloud.Utils.LoadPictureToView;
import erebus.sincloud.Utils.UploadFirebase;
import pub.devrel.easypermissions.EasyPermissions;

public class UserProfileActivity extends AppCompatActivity
{
    private static final String COMPLETED_ONBOARDING = "UserProfileActivity";
    private TextView nicknameTxtView;
    private FragmentAdapter pageAdapter;
    private int MAX_NICKNAME_CHARACTER_LIMIT = 20;
    private final int GALLERY_IMAGE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ImageView backToolbarButton = findViewById(R.id.activity_user_profile_back);
        nicknameTxtView = findViewById(R.id.activity_user_profile_nickname);
        backToolbarButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        TabLayout tabLayout = findViewById(R.id.activity_user_profile_tabs);
        ViewPager viewPager = findViewById(R.id.activity_user_profile_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        pageAdapter = new FragmentAdapter(fragmentManager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabNames(tabLayout);

        loadProfilePic();
        loadUserInfo();
        setupNicknameListener();
        setupProfilePicListener();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPreferences.getBoolean(COMPLETED_ONBOARDING, false))
        {
            // This is the first time running the app, let's go to onboarding
            displayOnboarding();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        SinAudioPlayer.getInstance().stopPlayback();
    }

    private void setupNicknameListener()
    {
        nicknameTxtView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Create an alert dialog to get title
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams")
                View alertLayout = inflater.inflate(R.layout.alert_dialog_nickname, null);
                final TextView charactersLeft = alertLayout.findViewById(R.id.alert_dialog_characters_left);
                final TextView charactersTotal = alertLayout.findViewById(R.id.alert_dialog_total_characters);
                final String charLimit = "/  " + MAX_NICKNAME_CHARACTER_LIMIT;
                charactersTotal.setText(charLimit);
                final TextInputEditText input = alertLayout.findViewById(R.id.alert_dialog_nickname_text);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_NICKNAME_CHARACTER_LIMIT) });

                charactersLeft.setText(String.valueOf(MAX_NICKNAME_CHARACTER_LIMIT));
                input.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after)
                    {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {

                    }

                    @Override
                    public void afterTextChanged(Editable s)
                    {
                        charactersLeft.setText(String.valueOf(MAX_NICKNAME_CHARACTER_LIMIT - s.length()));
                    }
                });

                final AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                builder.setTitle(getString(R.string.setup_nickname_dialog_promt));
                builder.setView(alertLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String sinName = input.getText().toString();
                        if(sinName.length() == 0)
                        {
                            return;
                        }
                        // Store nickname to firebase
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(user).getUid()).child("nickname");
                        userRef.setValue(sinName);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void loadUserInfo()
    {
        final TextView sinsNumber = findViewById(R.id.activity_user_profile_sins_number);
        final TextView commentNumber = findViewById(R.id.activity_user_profile_comment_number);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Object nicknameObj = dataSnapshot.child("nickname").getValue();
                    if(nicknameObj != null)
                    {
                        nicknameTxtView.setText(nicknameObj.toString());
                    }
                    else
                    {
                        nicknameTxtView.setText(getString(R.string.click_to_change_username));
                        nicknameTxtView.setTextColor(getResources().getColor(R.color.md_blue_900));
                    }

                    sinsNumber.setText(String.valueOf(dataSnapshot.child("sins").getChildrenCount()));
                    Object commentsObj = dataSnapshot.child("comments").getValue();
                    if(commentsObj != null)
                    {
                        commentNumber.setText(String.valueOf(commentsObj));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }

    // Open intent for uploading picture
    private void setupProfilePicListener()
    {
        CircleImageView profilePic = findViewById(R.id.activity_user_profile_image);
        profilePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                requestStoragePermissions();
            }
        });
    }

    private void loadProfilePic()
    {
        CircleImageView profilePic = findViewById(R.id.activity_user_profile_image);
        LoadPictureToView profileImageLoader = new LoadPictureToView();
        profileImageLoader.GetAndLoadProfilePictureToView(this, profilePic);
    }

    private void setupTabNames(TabLayout tabLayout)
    {
        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(R.string.my_sins);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(R.string.history);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        pageAdapter.addFragment(new SinsViewFragment(), "My Sins");
        pageAdapter.addFragment(new HistoryFragment(), "History");
        viewPager.setAdapter(pageAdapter);
    }

    private void requestStoragePermissions()
    {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms))
        {
            openGalleryIntent();
        }
        else
        {
            // Do not have permissions, request them now
            int PERMISSIONS_REQUEST_STORAGE = 32;
            EasyPermissions.requestPermissions(this, "SinCloud needs to access your external storage!", PERMISSIONS_REQUEST_STORAGE, perms);
        }
    }

    private void openGalleryIntent()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Bitmap bitmap = null;
        CircleImageView profilePic = findViewById(R.id.activity_user_profile_image);

        try
        {
            if(data.getData() == null)
            {
                throw new Exception();
            }
            InputStream is = getContentResolver().openInputStream(data.getData());
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
        }
        catch (Exception e)
        {
            Toast.makeText(UserProfileActivity.this, getString(R.string.media_photo_error_message), Toast.LENGTH_SHORT).show();
        }
        if (resultCode == RESULT_OK)
        {
            if (bitmap != null)
            {
                // Upload picture to server
                UploadFirebase uploadFirebase = new UploadFirebase();
                uploadFirebase.UploadProfilePicFirebase(bitmap, UserProfileActivity.this);
                profilePic.setImageBitmap(bitmap);
            }
            else
            {
                Toast.makeText(UserProfileActivity.this, getString(R.string.media_photo_error_message), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void displayOnboarding()
    {
        TapTargetSequence tapSequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.activity_user_profile_nickname), getString(R.string.user_profile_onboarding_nickname_title), getString(R.string.user_profile_onboarding_nickname_text))
                                .outerCircleColor(R.color.md_blue_A700)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.95f)            // Specify the alpha amount for the outer circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.md_white_1000)      // Specify the color of the title text
                                .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                .textColor(R.color.md_black_1000)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.md_black_1000)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(54),
                        TapTarget.forView(findViewById(R.id.activity_user_profile_image), getString(R.string.user_profile_onboarding_profilepic_title), getString(R.string.user_profile_onboarding_profilepic_text))
                                .outerCircleColor(R.color.md_blue_A700)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.95f)            // Specify the alpha amount for the outer circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.md_white_1000)      // Specify the color of the title text
                                .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                .targetCircleColor(R.color.md_red_400)
                                .textColor(R.color.md_black_1000)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.md_black_1000)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(54)
                );
        tapSequence.start();

        // User has seen OnBoarding, so mark our SharedPreferences
        // flag as completed.
        SharedPreferences.Editor sharedPreferencesEditor;
        sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        sharedPreferencesEditor.putBoolean(COMPLETED_ONBOARDING, true);
        sharedPreferencesEditor.apply();
    }
}
