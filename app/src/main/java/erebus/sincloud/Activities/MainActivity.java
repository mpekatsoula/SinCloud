package erebus.sincloud.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;
import erebus.sincloud.Adapters.FragmentAdapter;
import erebus.sincloud.Fragments.DiscoverFragment;
import erebus.sincloud.Fragments.NotificationsFragment;
import erebus.sincloud.Fragments.TrendingFragment;
import erebus.sincloud.R;
import erebus.sincloud.Singletons.SinAudioPlayer;
import erebus.sincloud.Utils.ForceUpdateChecker;
import erebus.sincloud.Utils.LoadPictureToView;

public class MainActivity extends AppCompatActivity implements RatingDialogListener, ForceUpdateChecker.OnUpdateNeededListener
{
    private static final String COMPLETED_ONBOARDING = "MainActivity";
    private FragmentAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();

        Toolbar toolbar = findViewById(R.id.main_activity_toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.main_activity_tabs);
        ViewPager viewPager = findViewById(R.id.main_activity_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();
        pageAdapter = new FragmentAdapter(fragmentManager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabNames(tabLayout);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), RecordSinActivity.class);
                startActivity(intent);
            }
        });

        // Load profile picture
        CircleImageView profilePicToolbar = findViewById(R.id.main_activity_profile_image);
        LoadPictureToView profileImageLoader = new LoadPictureToView();
        profileImageLoader.GetAndLoadProfilePictureToView(this, profilePicToolbar);
        profilePicToolbar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openUserProfileActivity();
            }
        });

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

    private void setupTabNames(TabLayout tabLayout)
    {
        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(R.string.trending);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(R.string.discover);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setText(R.string.notifications);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        pageAdapter.addFragment(new TrendingFragment(), "Popular");
        pageAdapter.addFragment(new DiscoverFragment(), "Discover");
        pageAdapter.addFragment(new NotificationsFragment(), "Notifications");
        viewPager.setAdapter(pageAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_main_action_feedback:
                feedback();
                return true;
            case R.id.menu_main_action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void feedback()
    {
        new AppRatingDialog.Builder()
                .setPositiveButtonText(R.string.submit)
                .setNegativeButtonText(R.string.cancel)
                .setNeutralButtonText(R.string.later)
                .setNoteDescriptions(Arrays.asList(getString(R.string.very_bad), getString(R.string.not_good), getString(R.string.quite_ok), getString(R.string.very_good), getString(R.string.excellent)))
                .setDefaultRating(2)
                .setTitle(R.string.feedback_title)
                .setDescription(R.string.feedback_description)
                .setCommentInputEnabled(true)
                .setHint(R.string.feedback_hint)
                .setCommentBackgroundColor(R.color.md_grey_400)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(MainActivity.this)
                .show();
    }

    private void openUserProfileActivity()
    {
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        startActivity(intent);
    }

    private void logout()
    {
        FirebaseAuth.getInstance().signOut();
        finish();

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void displayOnboarding()
    {
        TapTargetView.showFor(this,
                TapTarget.forView(findViewById(R.id.fab), getString(R.string.main_activity_onboarding_title), getString(R.string.main_activity_onboarding_text))
                        .outerCircleColor(R.color.md_blue_A700)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.95f)            // Specify the alpha amount for the outer circle
                        .titleTextSize(25)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.md_white_1000)      // Specify the color of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .textColor(R.color.md_black_1000)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.md_black_1000)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(54));

        // User has seen OnBoarding, so mark our SharedPreferences
        // flag as completed.
        SharedPreferences.Editor sharedPreferencesEditor;
        sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit();
        sharedPreferencesEditor.putBoolean(COMPLETED_ONBOARDING, true);
        sharedPreferencesEditor.apply();
    }

    @Override
    public void onNegativeButtonClicked()
    {

    }

    @Override
    public void onNeutralButtonClicked()
    {

    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String s)
    {
        // Store feedback to firebase
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference().child("feedback").push();
        feedbackRef.child("feedback").setValue(s);
        feedbackRef.child("uid").setValue(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        feedbackRef.child("stars").setValue(i);

        // Ask for play store
        if(i == 5)
        {
            // TODO
        }
    }
    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.new_version_title))
                .setMessage(getString(R.string.new_version_text))
                .setPositiveButton(getString(R.string.update),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton(getString(R.string.no_thanks),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
