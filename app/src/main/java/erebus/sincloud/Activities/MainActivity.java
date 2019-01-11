package erebus.sincloud.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
import erebus.sincloud.Utils.LoadPictureToView;

public class MainActivity extends AppCompatActivity implements RatingDialogListener
{
    private FragmentAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(2)
                .setTitle("Provide feedback about SinCloud")
                .setDescription("How did you find the overall experience?")
                .setCommentInputEnabled(true)
                .setHint("Please write your comment here ...")
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
    }
}
