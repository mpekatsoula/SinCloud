package erebus.sincloud.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;
import erebus.sincloud.Adapters.MainActivityAdapter;
import erebus.sincloud.Fragments.DiscoverFragment;
import erebus.sincloud.Fragments.PopularFragment;
import erebus.sincloud.R;
import erebus.sincloud.Utils.LoadPictureToView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity
{
    private MainActivityAdapter pageAdapter;

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
        pageAdapter = new MainActivityAdapter(fragmentManager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        Button settingsButton = findViewById(R.id.main_activity_settings);
        settingsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

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
        profileImageLoader.LoadProfilePictureToView(this, profilePicToolbar);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        pageAdapter.addFragment(new PopularFragment(), "Popular");
        pageAdapter.addFragment(new DiscoverFragment(), "Discover");
//        pageAdapter.addFragment(new PopularFragment(), "Popular 2");
//        pageAdapter.addFragment(new PopularFragment(), "Popular 3");
        viewPager.setAdapter(pageAdapter);
    }
}
