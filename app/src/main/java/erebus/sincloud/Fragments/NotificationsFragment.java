package erebus.sincloud.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Helpers.UpdateLikeStatus;
import erebus.sincloud.Listeners.LikeButtonListener;
import erebus.sincloud.Listeners.PlayButtonListener;
import erebus.sincloud.Listeners.SinsRecycleViewInnerLayoutListener;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.UI.SinsMenuAdapter;

public class NotificationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "NotificationsFragment";
    private SinsMenuAdapter mAdapter;
    private ArrayList<Sin> sinsArray = new ArrayList<>();
    private ArrayList<String> sinsRefs = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private UpdateLikeStatus updateLikeStatus = null;
    private long scoreCounter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        RecyclerView mRecyclerView = view.findViewById(R.id.trending_fragment_recycle_view);
        mSwipeRefreshLayout = view.findViewById(R.id.trending_fragment_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mAdapter = new SinsMenuAdapter(sinsArray, sinsRefs, SinMenuAdapterTypes.NOTIFICATIONS);
        mAdapter.setInnerConstraintLayoutClickListener(new SinsRecycleViewInnerLayoutListener(this.getContext(), mAdapter));
        mAdapter.setPlayClickListener(new PlayButtonListener(mAdapter, SinMenuAdapterTypes.NOTIFICATIONS));
        mAdapter.setLikeClickListener(new LikeButtonListener(mAdapter, SinMenuAdapterTypes.NOTIFICATIONS));
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext())
        {
            @Override
            public void onLayoutCompleted(RecyclerView.State state)
            {
                super.onLayoutCompleted(state);
                updateLikeStatus.Update();

            }
        };

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        updateLikeStatus = new UpdateLikeStatus(mAdapter, manager, this.getContext(), SinMenuAdapterTypes.NOTIFICATIONS);

        // Get the list of the latest sins
        getNotifications();
    }

    private void getNotifications()
    {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(user).getUid()).child("sins");

        // Get user's sins
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                sinsArray.clear();
                sinsRefs.clear();
                final long numElements = dataSnapshot.getChildrenCount();

                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    if(data.getKey() != null)
                    {
                        DatabaseReference newRef = FirebaseDatabase.getInstance().getReference().child("notify").child("sin").child(data.getKey());
                        newRef.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                Object flagNew = dataSnapshot.getValue();
                                if(flagNew != null && dataSnapshot.getKey() != null && (boolean)flagNew)
                                {
                                    // Get the actual sin
                                    DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(dataSnapshot.getKey());
                                    sinRef.addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            scoreCounter++;
                                            Sin new_sin = dataSnapshot.getValue(Sin.class);
                                            sinsArray.add(new_sin);
                                            sinsRefs.add(dataSnapshot.getKey());

                                            // Notify adapter only when we have all the data
                                            if(scoreCounter == numElements)
                                            {
                                                mAdapter.notifyDataSetChanged();
                                                updateLikeStatus.Update();
                                                scoreCounter = 0;
                                            }
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
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh()
    {
        getNotifications();
    }
}
