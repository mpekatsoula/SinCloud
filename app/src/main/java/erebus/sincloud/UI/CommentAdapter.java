package erebus.sincloud.UI;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import erebus.sincloud.Activities.DisplaySinActivity;
import erebus.sincloud.Models.Comment;
import erebus.sincloud.R;
import erebus.sincloud.Utils.LoadPictureToView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>
{
    private ArrayList<Comment> commentsDataset;
    private ArrayList<String> commentsRefs;
    private String sinRefString;
    private String TAG = "CommentAdapter";
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView imageViewProfile;
        TextView usernameTextView;
        TextView commentTextView;
        TextView messageTimeTextView;
        TextView likesTextView;
        Button likeCommentButton;

        ViewHolder(View v)
        {
            super(v);
            imageViewProfile = v.findViewById(R.id.comment_layout_user_photo);
            usernameTextView = v.findViewById(R.id.comment_layout_username);
            commentTextView = v.findViewById(R.id.comment_layout_text);
            messageTimeTextView = v.findViewById(R.id.comment_layout_time);
            likesTextView = v.findViewById(R.id.comment_layout_likes_textview);
            likeCommentButton = v.findViewById(R.id.comment_layout_like_button);
        }
    }

    public CommentAdapter(ArrayList<Comment> commentsDataset, ArrayList<String> commentsRefs, String sinRefString)
    {
        this.commentsDataset = commentsDataset;
        this.commentsRefs = commentsRefs;
        this.sinRefString = sinRefString;
        Log.d(TAG, "Constructor()");
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        Log.d(TAG, "onCreateViewHolder()");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        context = itemView.getContext();

        return new CommentAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ViewHolder holder, final int position)
    {
        final Comment comment = commentsDataset.get(position);
        // Get data from firebase
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(comment.getUsername());
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Object photoURLObject = dataSnapshot.child("photoURL").getValue();

                // Load profile picture
                LoadPictureToView profileImageLoader = new LoadPictureToView();
                if(photoURLObject != null)
                {
                    profileImageLoader.LoadProfilePictureToView(context, holder.imageViewProfile, photoURLObject.toString());
                }
                else
                {
                    profileImageLoader.LoadProfilePictureToView(context,  holder.imageViewProfile, "https://d50m6q67g4bn3.cloudfront.net/avatars/f38ca4f8-4cf8-4d43-b54f-a46c417aadd7_1519597926301");
                }

                Object usernameObject = dataSnapshot.child("displayName").getValue();
                if(usernameObject != null)
                {
                    holder.usernameTextView.setText(usernameObject.toString());
                }
                else
                {
                    holder.usernameTextView.setText(context.getString(R.string.anonymous));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        holder.commentTextView.setText(comment.getComment());

        // Convert time to the correct timezone before displaying.
        // If message is older than a day, display date.
        long messageLocalTime = comment.getMessageTimeLong() + TimeZone.getDefault().getRawOffset();
        long timeDiff = ( System.currentTimeMillis() - messageLocalTime) / (1000*60*60*24);
        if (timeDiff > 1 )
        {
            holder.messageTimeTextView.setText(DateFormat.format("dd/MM", comment.getMessageTimeLong()));
        }
        else
        {
            holder.messageTimeTextView.setText(DateFormat.format("HH:mm", comment.getMessageTimeLong()));
        }

        holder.likesTextView.setText(String.valueOf(comment.getLikes()));

        holder.likeCommentButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Check if the user has liked the comment before.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null)
                {
                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("clikes").child(comment.getKey());
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
                            userRef.setValue(!likedStatus);

                            final DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference().child("comments").child(sinRefString).child(comment.getKey()).child("likes");
                            // Increase/decrease the like counter
                            if(!likedStatus)
                            {
                                comment.setLikes(comment.getLikes() + 1);
                                holder.likesTextView.setText(String.valueOf(comment.getLikes()));
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
                                comment.setLikes(comment.getLikes() - 1);
                                holder.likesTextView.setText(String.valueOf(comment.getLikes()));
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
    public int getItemCount()
    {
        return commentsDataset.size();
    }
}
