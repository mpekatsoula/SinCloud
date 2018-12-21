package erebus.sincloud.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Listeners.PlayButtonListener;
import erebus.sincloud.Listeners.SinsRecycleViewInnerLayoutListener;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Singletons.SinAudioPlayer;
import erebus.sincloud.UI.SinsMenuAdapter;

public class DiscoverFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "DiscoverFragment";
    private SinsMenuAdapter mAdapter;
    private ArrayList<Sin> sinsArray = new ArrayList<>();
    private ArrayList<String> sinsRefs = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        RecyclerView mRecyclerView = view.findViewById(R.id.discover_fragment_recycle_view);
        mSwipeRefreshLayout = view.findViewById(R.id.discover_fragment_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mAdapter = new SinsMenuAdapter(sinsArray, sinsRefs, SinMenuAdapterTypes.DISCOVER);
        mAdapter.setInnerConstraintLayoutClickListener(new SinsRecycleViewInnerLayoutListener(this.getContext(), mAdapter));
        mAdapter.setPlayClickListener(new PlayButtonListener(mAdapter));

        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Get the list of the latest sins
        getLatestSins();
    }

    private void getLatestSins()
    {
        final DatabaseReference sightedUsersRef = FirebaseDatabase.getInstance().getReference().child("sins");
        sightedUsersRef.orderByChild("sinTime");
        sightedUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                sinsArray.clear();
                sinsRefs.clear();
                mAdapter.notifyDataSetChanged();
                for(DataSnapshot sin : dataSnapshot.getChildren())
                {
                    Sin new_sin = sin.getValue(Sin.class);
                    sinsArray.add(new_sin);
                    sinsRefs.add(sin.getKey());
                }
                Collections.reverse(sinsArray);
                Collections.reverse(sinsRefs);
                mAdapter.notifyDataSetChanged();
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
        getLatestSins();
    }
}
