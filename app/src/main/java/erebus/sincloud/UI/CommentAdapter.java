package erebus.sincloud.UI;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import erebus.sincloud.Activities.DisplaySinActivity;
import erebus.sincloud.Models.Comment;
import erebus.sincloud.R;

public class CommentAdapter extends FirebaseListAdapter<Comment>
{
    private DisplaySinActivity activity;
    private String sinRefString;
    private String TAG = "CommentAdapter";

    public CommentAdapter(FirebaseListOptions<Comment> options, DisplaySinActivity activity, String sinRefString)
    {
        super(options);
        this.activity = activity;
        this.sinRefString = sinRefString;
        Log.d(TAG, "Constructor()");
    }

    @Override
    protected void populateView(final View v, final Comment model, int position)
    {
        // Get data from firebase
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(model.getUsername());
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                final ImageView imageViewProfile = v.findViewById(R.id.comment_layout_user_photo);
                Object photoURLObject = dataSnapshot.child("photoURL").getValue();
                if(photoURLObject != null)
                {
                    Glide.
                            with(v.getContext()).
                            load(photoURLObject.toString()).
                            into(imageViewProfile);
                }
                else
                {
                    Glide.
                            with(v.getContext()).
                            load("https://d50m6q67g4bn3.cloudfront.net/avatars/f38ca4f8-4cf8-4d43-b54f-a46c417aadd7_1519597926301").
                            into(imageViewProfile);
                }

                TextView usernameTextView = v.findViewById(R.id.comment_layout_username);
                Object userameObject = dataSnapshot.child("username").getValue();
                if(userameObject != null)
                {
                    usernameTextView.setText(userameObject.toString());
                }
                else
                {
                    usernameTextView.setText("Anonymous");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        TextView commentTextView = v.findViewById(R.id.comment_layout_text);
        commentTextView.setText(model.getComment());

        // Convert time to the correct timezone before displaying.
        // If message is older than a day, display date.
        TextView messageTime = v.findViewById(R.id.comment_layout_time);
        long messageLocalTime = model.getMessageTimeLong() + TimeZone.getDefault().getRawOffset();
        long timeDiff = ( System.currentTimeMillis() - messageLocalTime) / (1000*60*60*24);
        if (timeDiff > 1 )
        {
            messageTime.setText(DateFormat.format("dd/MM", model.getMessageTimeLong()));
        }
        else
        {
            messageTime.setText(DateFormat.format("HH:mm", model.getMessageTimeLong()));
        }

        final TextView likesTextView = v.findViewById(R.id.comment_layout_likes_textview);
        likesTextView.setText(String.valueOf(model.getLikes()));

        Button likeCommentButton = v.findViewById(R.id.comment_layout_like_button);
        likeCommentButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Check if the user has liked the sin before.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null)
                {
                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("clikes").child(model.getKey());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            Object likedStatusObject = dataSnapshot.getValue();
                            boolean likedStatus = false;
                            if(likedStatusObject != null)
                            {
                                likedStatus = (boolean) likedStatusObject;
                            }
                            final DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference().child("comments").child(sinRefString).child(model.getKey()).child("likes");
                            userRef.setValue(!likedStatus);

                            // Increase/decrease the like counter
                            if(!likedStatus)
                            {
                                likesTextView.setText(String.valueOf(model.getLikes() + 1));
                                commentRef.runTransaction(new Transaction.Handler()
                                {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData)
                                    {
                                        mutableData.setValue(Long.parseLong(mutableData.getValue().toString()) + 1);
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot)
                                    {

                                    }
                                });
                            }
                            else
                            {
                                likesTextView.setText(String.valueOf(model.getLikes() - 1));
                                commentRef.runTransaction(new Transaction.Handler()
                                {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData)
                                    {
                                        mutableData.setValue(Long.parseLong(mutableData.getValue().toString()) - 1);
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot)
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
        });
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        Comment comment = getItem(position);
        Log.d(TAG, "getView!");

        if(view == null)
        {
            view = activity.getLayoutInflater().inflate(R.layout.comment_layout, viewGroup, false);
        }

        // Generating view
        populateView(view, comment, position);

        return view;
    }

    @Override
    public Comment getItem(int position)
    {
        // Reverse list
        return super.getItem(this.getCount() - position - 1);
    }
}
