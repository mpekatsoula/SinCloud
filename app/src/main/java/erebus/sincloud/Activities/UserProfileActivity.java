package erebus.sincloud.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class UserProfileActivity extends AppCompatActivity
{
    private TextView nicknameTxtView;
    private FragmentAdapter pageAdapter;
    private int MAX_NICKNAME_CHARACTER_LIMIT = 10;
    private AlertDialog changeUsernameDialog;

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
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        SinAudioPlayer.getInstance().stopPlayback();
    }

    private void setupNicknameListener()
    {
        // Set up the input
        final EditText input = new EditText(UserProfileActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_NICKNAME_CHARACTER_LIMIT) });

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
//                        TextView tv = findViewById(R.id.yourTextViewId);
//                        tv.setText(String.valueOf(MAX_CHARACTER_LIMIT - s.length()));
            }
        });

        changeUsernameDialog = new AlertDialog.Builder(UserProfileActivity.this)
                .setTitle(getString(R.string.setup_nickname_dialog_promt))
                .setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener()
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
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                })
                .create();

        nicknameTxtView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeUsernameDialog.show();
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
                    commentNumber.setText(String.valueOf(dataSnapshot.child("scomments").getChildrenCount()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
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
}
