package erebus.sincloud.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Listeners.DeleteButtonListener;
import erebus.sincloud.Listeners.PlayButtonListener;
import erebus.sincloud.Listeners.SinsRecycleViewInnerLayoutListener;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.UI.SinsMenuAdapter;

public class SinsViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "SinsViewFragment";
    private SinsMenuAdapter mAdapter;
    private ArrayList<Sin> sinsArray = new ArrayList<>();
    private ArrayList<String> sinsRefs = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sins_view, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        RecyclerView mRecyclerView = view.findViewById(R.id.sins_view_fragment_recycle_view);
        mSwipeRefreshLayout = view.findViewById(R.id.sins_view_fragment_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mAdapter = new SinsMenuAdapter(sinsArray, sinsRefs, SinMenuAdapterTypes.USER_SETTINGS);
        mAdapter.setDeleteClickListener(new DeleteButtonListener(mAdapter));
        mAdapter.setInnerConstraintLayoutClickListener(new SinsRecycleViewInnerLayoutListener(this.getContext(), mAdapter));
        mAdapter.setPlayClickListener(new PlayButtonListener(mAdapter));

        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Get the list of my sins
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
                sinsArray.clear();
                sinsRefs.clear();
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
                            mSwipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
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
        getSins();
    }
}
