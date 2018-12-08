package erebus.sincloud.UI;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Utils.AudioPlayer;

public class SinsMenuAdapter extends RecyclerView.Adapter<SinsMenuAdapter.ViewHolder>
{
    private ArrayList<Sin> mDataset;
    private String TAG = "SinsMenuAdapter";
    private MediaPlayer mediaPlayer = null;
    private AudioPlayer audioPlayer = null;
    private int previousHolderPosition = -1;
    private int currentPlayingPosition = -1;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView titleTxtView;
        TextView commentsTxtView;
        TextView likesTxtView;
        TextView timeTxtView;
        Button playButton;
        private boolean isPlaying = false;

        ViewHolder(View v)
        {
            super(v);
            titleTxtView = v.findViewById(R.id.sin_view_title);
            likesTxtView = v.findViewById(R.id.sin_view_likes);
            commentsTxtView = v.findViewById(R.id.sin_view_comments);
            timeTxtView = v.findViewById(R.id.sin_view_time);
            playButton = v.findViewById(R.id.sin_view_play_button);
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
    public SinsMenuAdapter(ArrayList<Sin> myDataset)
    {
        Log.d(TAG, "SinsMenuAdapter()");
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public SinsMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        Log.d(TAG, "onCreateViewHolder()");
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sin_view, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        Log.d(TAG, "onBindViewHolder() position: " + position);

        // If case we get notified and we were playing an audio file before
        // toggle the play button.
        if(currentPlayingPosition != -1 && currentPlayingPosition != holder.getAdapterPosition())
        {
            holder.setPlayButtonSrc(true);
            previousHolderPosition = currentPlayingPosition;
        }

        final Sin sinInfo = mDataset.get(position);

        holder.titleTxtView.setText(sinInfo.getTitle());
        holder.commentsTxtView.setText(String.valueOf(sinInfo.getComments()));
        holder.likesTxtView.setText(String.valueOf(sinInfo.getLikes()));
        holder.timeTxtView.setText(String.valueOf(sinInfo.getTime()));

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
                audioPlayer.execute(sinInfo.getUrl());
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
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }
}