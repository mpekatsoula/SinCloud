package erebus.sincloud.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Listeners.PlayButtonListener;
import erebus.sincloud.Listeners.SinMenuListener;
import erebus.sincloud.Listeners.SinsRecycleViewInnerLayoutListener;
import erebus.sincloud.Listeners.onRecycleViewClickListener;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.UI.SinsMenuAdapter;

public class TrendingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "TrendingFragment";
    private SinsMenuAdapter mAdapter = null;
    private ArrayList<Sin> sinsArray = new ArrayList<>();
    private ArrayList<String> sinsRefs = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private int scoreCounter = 0;

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

        mAdapter = new SinsMenuAdapter(sinsArray, sinsRefs, SinMenuAdapterTypes.TRENDING);
        mAdapter.setInnerConstraintLayoutClickListener(new SinsRecycleViewInnerLayoutListener(this.getContext(), mAdapter));
        mAdapter.setPlayClickListener(new PlayButtonListener(mAdapter, SinMenuAdapterTypes.TRENDING));
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Get the list of the latest sins
        getTrendingSins();
    }

    private void getTrendingSins()
    {
        final DatabaseReference sightedUsersRef = FirebaseDatabase.getInstance().getReference().child("trending");
        sightedUsersRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                sinsArray.clear();
                sinsRefs.clear();

                final List<Pair<String, Double>> scores = new ArrayList<>();
                for(DataSnapshot sin : dataSnapshot.getChildren())
                {
                    Object sinKey = sin.child("key").getValue();
                    Object score = sin.child("score").getValue();
                    if(sinKey != null && score != null)
                    {
                        scores.add(new Pair<>(sinKey.toString(), Double.valueOf(score.toString())));
                    }
                }

                // Sort scores
                Collections.sort(scores, new Comparator<Pair<String, Double>>() {
                    @Override
                    public int compare(final Pair<String, Double> o1, final Pair<String, Double> o2)
                    {
                        if (o1.second < o2.second)
                        {
                            return -1;
                        }
                        else if (o1.second.equals(o2.second))
                        {
                            return 0;
                        }
                        else
                        {
                            return 1;
                        }
                    }
                });

                for(int i = 0; i < scores.size(); ++i)
                {
                    DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(scores.get(i).first);
                    sinRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            // Get the sin data and display it
                            Sin new_sin = dataSnapshot.getValue(Sin.class);

                            // Insert elements in sorted order
                            int idx = 0;
                            sinsArray.add(idx, new_sin);
                            sinsRefs.add(idx, dataSnapshot.getKey());
                            scoreCounter++;

                            // Notify adapter only when you have all the data
                            if(scoreCounter == scores.size())
                            {
                                mAdapter.notifyDataSetChanged();
                                scoreCounter = 0;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
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
        getTrendingSins();
    }
}
