package erebus.sincloud.UI;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Utils.AudioPlayer;
import erebus.sincloud.Utils.TimeUtils;

public class SinsMenuAdapter extends RecyclerView.Adapter<SinsMenuAdapter.ViewHolder>
{
    private ArrayList<Sin> mDataset;
    private ArrayList<String> sinsRefs;
    private String TAG = "SinsMenuAdapter";
    private MediaPlayer mediaPlayer = null;
    private AudioPlayer audioPlayer = null;
    private int previousHolderPosition = -1;
    private int currentPlayingPosition = -1;
    private SinMenuAdapterTypes adapterType;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView titleTxtView;
        TextView commentsTxtView;
        TextView likesTxtView;
        TextView dateTxtView;
        TextView nicknameTxtView;
        Button playButton;
        Button deleteButton = null;
        private boolean isPlaying = false;

        ViewHolder(View v, SinMenuAdapterTypes adapterType)
        {
            super(v);
            if(adapterType == SinMenuAdapterTypes.USER_SETTINGS)
            {
                titleTxtView = v.findViewById(R.id.sin_view_user_title);
                likesTxtView = v.findViewById(R.id.sin_view_user_likes);
                commentsTxtView = v.findViewById(R.id.sin_view_user_comments);
                dateTxtView = v.findViewById(R.id.sin_view_user_date);
                playButton = v.findViewById(R.id.sin_view_user_play_button);
                deleteButton = v.findViewById(R.id.sin_view_user_delete_button);
            }
            else
            {
                titleTxtView = v.findViewById(R.id.sin_view_title);
                likesTxtView = v.findViewById(R.id.sin_view_likes);
                commentsTxtView = v.findViewById(R.id.sin_view_comments);
                dateTxtView = v.findViewById(R.id.sin_view_date);
                playButton = v.findViewById(R.id.sin_view_play_button);
                nicknameTxtView = v.findViewById(R.id.sin_view_nickname);
            }
        }

        private void setPlayButtonSrc(boolean playing)
        {
            Log.d("SinsMenuAdapter", "setPlayButtonSrc " + playing);
            if(playing)
            {
                playButton.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
            }
            else
            {
                playButton.setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24px);
            }
        }
    }

    // Constructor
    public SinsMenuAdapter(ArrayList<Sin> sinsDataset, ArrayList<String> sinsRefs, SinMenuAdapterTypes userSettings)
    {
        Log.d(TAG, "SinsMenuAdapter()");
        mDataset = sinsDataset;
        adapterType = userSettings;
        this.sinsRefs = sinsRefs;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public SinsMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        Log.d(TAG, "onCreateViewHolder()");
        View itemView;
        switch (adapterType)
        {
            case DISCOVER:
            case TRENDING:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sin_view, parent, false);
                break;
            case USER_SETTINGS:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sin_view_user, parent, false);
                break;
            default:
                itemView = null;
        }
        return new ViewHolder(itemView, adapterType);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
    {
        Log.d(TAG, "onBindViewHolder() position: " + position);

        // If case we get notified and we were playing an audio file before
        // toggle the play button.
        if(currentPlayingPosition != -1 && currentPlayingPosition != holder.getAdapterPosition())
        {
            holder.setPlayButtonSrc(true);
            previousHolderPosition = currentPlayingPosition;
        }

        final Sin sin = mDataset.get(position);

        holder.titleTxtView.setText(sin.getTitle());
        holder.commentsTxtView.setText(String.valueOf(sin.getComments()));
        holder.likesTxtView.setText(String.valueOf(sin.getLikes()));

        TimeUtils timeUtils = new TimeUtils();
        long timeDiff = timeUtils.ConvertServerTimeToLocal(sin.getMessageTimeLong());
        if (timeDiff > 1 )
        {
            holder.dateTxtView.setText(DateFormat.format("dd/MM", sin.getMessageTimeLong()));
        }
        else
        {
            holder.dateTxtView.setText(DateFormat.format("HH:mm", sin.getMessageTimeLong()));
        }

        // Set up listener for play button
        holder.playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mediaPlayer == null)
                {
                    mediaPlayer = new MediaPlayer();
                }
                else
                {
                    if(holder.isPlaying)
                    {
                        mediaPlayer.stop();
                        holder.setPlayButtonSrc(false);
                        return;
                    }
                    else
                    {
                        mediaPlayer.reset();
                        audioPlayer.cancel(true);
                        audioPlayer = null;
                    }
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
                audioPlayer = new AudioPlayer(mediaPlayer);
                audioPlayer.execute(sin.getUrl());
                holder.setPlayButtonSrc(false);
                currentPlayingPosition = holder.getAdapterPosition();
                if(previousHolderPosition == -1)
                {
                    previousHolderPosition = currentPlayingPosition;
                }
                // if the user hits a new play button while the previous hasn't stopped,
                // notify the previous view in order to reset it
                if(previousHolderPosition != currentPlayingPosition)
                {
                    Log.d(TAG, "Notifying " + previousHolderPosition);
                    notifyItemChanged(previousHolderPosition);
                }
            }
        });

        // Set listener for delete button
        if(adapterType == SinMenuAdapterTypes.USER_SETTINGS)
        {
            holder.deleteButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Are you sure you want to delete this sin?");
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            DatabaseReference sinUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(user).getUid()).child("sins").child(sinsRefs.get(position));
                            sinUserRef.removeValue();
                            DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinsRefs.get(position));
                            sinRef.removeValue();
                            sinsRefs.remove(position);
                            mDataset.remove(position);
                            notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                }
            });
        }

        // Load user's nickname
        if (adapterType == SinMenuAdapterTypes.TRENDING || adapterType == SinMenuAdapterTypes.DISCOVER)
        {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(sin.getUserid()).child("nickname");
            userRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Object nicknameObj = dataSnapshot.getValue();

                    if(nicknameObj == null)
                    {
                        holder.nicknameTxtView.setText("Anonymous");
                    }
                    else
                    {
                        holder.nicknameTxtView.setText(dataSnapshot.getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }
}