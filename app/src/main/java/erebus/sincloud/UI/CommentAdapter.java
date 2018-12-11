package erebus.sincloud.UI;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.TimeZone;

import erebus.sincloud.Activities.DisplaySinActivity;
import erebus.sincloud.Models.Comment;
import erebus.sincloud.R;

public class CommentAdapter extends FirebaseListAdapter<Comment>
{
    private DisplaySinActivity activity;
    private String uid;
    private String photoURL;
    private String TAG = "CommentAdapter";

    public CommentAdapter(FirebaseListOptions<Comment> options, DisplaySinActivity activity, String uid, String photoURL)
    {
        super(options);
        this.activity = activity;
        this.uid = uid;
        this.photoURL = photoURL;
        Log.d(TAG, "Constructor()");
    }

    @Override
    protected void populateView(View v, Comment model, int position)
    {
        final ImageView imageViewProfile = v.findViewById(R.id.comment_layout_user_photo);
        Glide.
                with(v.getContext()).
                load(photoURL).
                into(imageViewProfile);

        TextView usernameTextView = v.findViewById(R.id.comment_layout_username);
        usernameTextView.setText(model.getUsername());
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
}
