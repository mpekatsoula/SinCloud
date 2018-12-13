package erebus.sincloud.Activities;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.UI.SinsMenuAdapter;

public class UserProfileActivity extends AppCompatActivity
{
    private final String TAG = "UserProfileActivity";
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Sin> sinsArray = new ArrayList<>();
    private ArrayList<String> sinsRefs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        displaySins();
    }

    private void displaySins()
    {
        Log.d(TAG, "displaySins()");
        RecyclerView mRecyclerView = findViewById(R.id.activity_user_profile_recycleview);
        mAdapter = new SinsMenuAdapter(sinsArray, sinsRefs, SinMenuAdapterTypes.USER_SETTINGS);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        getSins();
    }

    private void getSins()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference sinsRef = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(user).getUid()).child("sins");
        sinsRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                // For each sin set sin data
                for(DataSnapshot sin : dataSnapshot.getChildren())
                {
                    final DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(Objects.requireNonNull(sin.getKey()));
                    sinRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            Sin new_sin = dataSnapshot.getValue(Sin.class);
                            sinsArray.add(new_sin);
                            sinsRefs.add(dataSnapshot.getKey());
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }
}
