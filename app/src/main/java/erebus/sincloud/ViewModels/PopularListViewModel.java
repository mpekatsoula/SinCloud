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

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class PopularListViewModel extends AndroidViewModel
{
    private final PopularListLiveData data;
    private static ArrayList<String> friendsData;
    private final String TAG = "FriendListVM";

    public PopularListViewModel(Application application)
    {
        super(application);
        data = new PopularListLiveData(application);
    }

    public LiveData<ArrayList<String>> getData()
    {
        return data;
    }

    private class PopularListLiveData extends LiveData<ArrayList<String>> implements ChildEventListener
    {
        private final DatabaseReference friendsRef;
        private final Context context;

        PopularListLiveData(Context context)
        {
            this.context = context;
            Log.d(TAG, "ShoutsLiveData() constructor is called");
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            friendsData = new ArrayList<>();
            friendsRef = FirebaseDatabase.getInstance().getReference("friends").child(user.getUid());
        }

        @Override
        protected void onActive()
        {
            super.onActive();
            friendsRef.addChildEventListener(this);
        }

        @Override
        protected void onInactive()
        {
            super.onInactive();
            friendsRef.removeEventListener(this);
        }


        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s)
        {
            Log.d(TAG, "onChildAdded() is called");
            Object friendPendingObj = dataSnapshot.child("pending").getValue();
            if (friendPendingObj != null && friendPendingObj.toString().equals("false"))
            {
                Object keyObj = dataSnapshot.getKey();
                if (keyObj != null && !friendsData.contains(keyObj.toString()))
                {
                    friendsData.add(keyObj.toString());
                }
            }
            super.setValue(friendsData);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s)
        {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot)
        {
            Log.d(TAG, "onChildRemoved() is called");
            Object keyObj = dataSnapshot.getKey();
            if (keyObj != null)
            {
                friendsData.remove(keyObj.toString());
            }
            super.setValue(friendsData);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s)
        {

        }

        @Override
        public void onCancelled(DatabaseError databaseError)
        {
        }
    }
}