package erebus.sincloud.ViewModels;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import erebus.sincloud.Models.Comment;

public class CommentsListViewModel extends AndroidViewModel
{
    private final CommentsListLiveData data;
    private static ArrayList<Comment> commentsData;
    private final String TAG = "CommentListVM";

    public CommentsListViewModel(Application application)
    {
        super(application);
        data = new CommentsListLiveData(application);
    }

    public LiveData<ArrayList<Comment>> getData()
    {
        return data;
    }

    private class CommentsListLiveData extends LiveData<ArrayList<Comment>> implements ChildEventListener
    {
        private final DatabaseReference commentsRef;
        private final Context context;

        CommentsListLiveData(Context context)
        {
            this.context = context;
            Log.d(TAG, "CommentsListLiveData constructor is called");

            commentsData = new ArrayList<>();
            commentsRef = FirebaseDatabase.getInstance().getReference("comments");
        }

        @Override
        protected void onActive()
        {
            super.onActive();
            commentsRef.addChildEventListener(this);
        }

        @Override
        protected void onInactive()
        {
            super.onInactive();
            commentsRef.removeEventListener(this);
        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s)
        {
            Log.d(TAG, "onChildAdded() is called " + dataSnapshot.toString());
            HashMap<String, Comment> keyObj = (HashMap<String, Comment>) dataSnapshot.getValue();

            super.setValue(commentsData);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s)
        {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
        {
            Log.d(TAG, "onChildRemoved() is called");
            Comment keyObj = (Comment) dataSnapshot.getValue();
            if (keyObj != null)
            {
                commentsData.remove(keyObj);
            }
            super.setValue(commentsData);
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