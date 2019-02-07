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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import erebus.sincloud.Models.Comment;
import erebus.sincloud.R;
import erebus.sincloud.Utils.LoadPictureToView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>
{
    private ArrayList<Comment> commentsDataset;
    private ArrayList<String> commentsRefs;
    private String sinRefString;
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
        ImageView likeCommentButton;

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
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        context = itemView.getContext();

        return new CommentAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ViewHolder holder, final int position)
    {
        final Comment comment = commentsDataset.get(position);

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

                // Change comment like color if the user has liked it before
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("clikes").child(sinRefString).child(comment.getKey()).child(user.getUid());
                likesRef.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        Object likedStatusObject = dataSnapshot.getValue();
                        if(likedStatusObject != null && (boolean) likedStatusObject)
                        {
                            holder.likeCommentButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccent));
                        }
                        else
                        {
                            holder.likeCommentButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.md_black_1000));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });

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
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null)
                {
                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("clikes").child(sinRefString).child(comment.getKey()).child(user.getUid());
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
                            // Increase/decrease the like counter
                            if(!likedStatus)
                            {
                                comment.setLikes(comment.getLikes() + 1);
                                holder.likesTextView.setText(String.valueOf(comment.getLikes()));
                                holder.likeCommentButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccent));
                            }
                            else
                            {
                                comment.setLikes(comment.getLikes() - 1);
                                holder.likesTextView.setText(String.valueOf(comment.getLikes()));
                                holder.likeCommentButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.md_black_1000));
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
