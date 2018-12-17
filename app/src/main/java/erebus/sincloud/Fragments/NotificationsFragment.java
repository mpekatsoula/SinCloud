package erebus.sincloud.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Listeners.SinMenuListener;
import erebus.sincloud.Listeners.onRecycleViewClickListener;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.UI.SinsMenuAdapter;

public class NotificationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String TAG = "NotificationsFragment";
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Sin> sinsArray = new ArrayList<>();
    private ArrayList<String> sinsRefs = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

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
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new SinMenuListener(view.getContext(), mRecyclerView, new onRecycleViewClickListener()
        {
            @Override
            public void onClick(View view, int position)
            {
                Sin userInfo = sinsArray.get(position);
                Log.d(TAG, "onCLick: " + userInfo.getTitle());

//                // Find the sin that the user selected and start activity
//                Intent intent = new Intent(getActivity(), DisplaySinActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("users_key", userInfo.getUid());
//                bundle.putString("users_photoURL", userInfo.getPhotoURL());
//                intent.putExtras(bundle);
//                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Get the list of the latest sins
        getNotifications();
    }

    private void getNotifications()
    {

    }

    @Override
    public void onRefresh()
    {

    }
}
