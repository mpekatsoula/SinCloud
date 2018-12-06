package erebus.sincloud.ViewModels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import erebus.sincloud.Models.Sin;

public class DiscoverListViewModel extends AndroidViewModel
{
    private final DiscoverListLiveData data;
    private static ArrayList<Sin> sinsData;
    private final String TAG = "DiscoverListVM";

    public DiscoverListViewModel(Application application)
    {
        super(application);
        data = new DiscoverListLiveData(application);
    }

    public LiveData<ArrayList<Sin>> getData()
    {
        return data;
    }

    private class DiscoverListLiveData extends LiveData<ArrayList<Sin>> implements ChildEventListener
    {
        private final DatabaseReference sinsRef;
        private final Context context;

        DiscoverListLiveData(Context context)
        {
            this.context = context;
            Log.d(TAG, "DiscoverListLiveData constructor is called");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            sinsData = new ArrayList<>();
            sinsRef = FirebaseDatabase.getInstance().getReference("sins");
        }

        @Override
        protected void onActive()
        {
            super.onActive();
            sinsRef.addChildEventListener(this);
        }

        @Override
        protected void onInactive()
        {
            super.onInactive();
            sinsRef.removeEventListener(this);
        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s)
        {
            Log.d(TAG, "onChildAdded() is called");
            Sin keyObj = (Sin) dataSnapshot.getValue();
            if (keyObj != null && !sinsData.contains(keyObj))
            {
                sinsData.add(keyObj);
            }
            super.setValue(sinsData);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s)
        {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
        {
            Log.d(TAG, "onChildRemoved() is called");
            Sin keyObj = (Sin) dataSnapshot.getValue();
            if (keyObj != null)
            {
                sinsData.remove(keyObj);
            }
            super.setValue(sinsData);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s)
        {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError)
        {
        }
    }
}