package erebus.sincloud.UI;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Utils.TimeUtils;

public class SinsMenuAdapter extends RecyclerView.Adapter<SinsMenuAdapter.ViewHolder>
{
    private ArrayList<Sin> mDataset;
    private ArrayList<String> sinsRefs;
    private SinMenuAdapterTypes adapterType;
    private View.OnClickListener playButtonViewOnClickListener;
    private View.OnClickListener likeButtonViewOnClickListener;
    private View.OnClickListener deleteButtonViewOnClickListener;
    private View.OnClickListener innerConstraintLayoutViewOnClickListener;

    public void setPlayClickListener(View.OnClickListener clickListener)
    {
        playButtonViewOnClickListener = clickListener;
    }
    public void setLikeClickListener(View.OnClickListener clickListener)
    {
        likeButtonViewOnClickListener = clickListener;
    }
    public void setDeleteClickListener(View.OnClickListener clickListener)
    {
        deleteButtonViewOnClickListener = clickListener;
    }
    public void setInnerConstraintLayoutClickListener(View.OnClickListener clickListener)
    {
        innerConstraintLayoutViewOnClickListener = clickListener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView titleTxtView;
        TextView commentsTxtView;
        TextView likesTxtView;
        TextView dateTxtView;
        TextView durationTxtView;
        TextView nicknameTxtView;
        ConstraintLayout innerConstraintLayout;
        ConstraintLayout innerConstraintLayout2;
        ImageView playButton;
        ImageView likeButton;
        ImageView deleteButton;

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
                innerConstraintLayout = v.findViewById(R.id.sin_view_user_inner_constraint_layout);
                innerConstraintLayout2 = v.findViewById(R.id.sin_view_user_inner_constraint_layout2);
                likeButton = v.findViewById(R.id.sin_view_user_like_image);
                deleteButton.setTag(this);
                deleteButton.setOnClickListener(deleteButtonViewOnClickListener);
            }
            else
            {
                titleTxtView = v.findViewById(R.id.sin_view_title);
                likesTxtView = v.findViewById(R.id.sin_view_likes);
                commentsTxtView = v.findViewById(R.id.sin_view_comments);
                dateTxtView = v.findViewById(R.id.sin_view_date);
                durationTxtView = v.findViewById(R.id.sin_view_duration);
                playButton = v.findViewById(R.id.sin_view_play_button);
                nicknameTxtView = v.findViewById(R.id.sin_view_nickname);
                innerConstraintLayout = v.findViewById(R.id.sin_view_inner_constraint_layout);
                innerConstraintLayout2 = v.findViewById(R.id.sin_view_inner_constraint_layout2);
                likeButton = v.findViewById(R.id.sin_view_like_image);
            }

            playButton.setTag(this);
            playButton.setOnClickListener(playButtonViewOnClickListener);
            likeButton.setTag(this);
            likeButton.setOnClickListener(likeButtonViewOnClickListener);
            innerConstraintLayout.setTag(this);
            innerConstraintLayout.setOnClickListener(innerConstraintLayoutViewOnClickListener);
            innerConstraintLayout2.setTag(this);
            innerConstraintLayout2.setOnClickListener(innerConstraintLayoutViewOnClickListener);
        }
    }

    // Constructor
    public SinsMenuAdapter(ArrayList<Sin> sinsDataset, ArrayList<String> sinsRefs, SinMenuAdapterTypes userSettings)
    {
        mDataset = sinsDataset;
        adapterType = userSettings;
        this.sinsRefs = sinsRefs;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public SinsMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView;
        switch (adapterType)
        {
            case DISCOVER:
            case TRENDING:
            case NOTIFICATIONS:
            case HISTORY:
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
        final Sin sin = mDataset.get(position);

        if(sin == null)
        {
            return;
        }

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
            holder.dateTxtView.setText("Today");
        }

        // Load user's nickname and set sin time
        if (adapterType != SinMenuAdapterTypes.USER_SETTINGS)
        {
            long totalSecs = sin.getTime() > 0 ? sin.getTime() : 1;
            long minutes = (totalSecs % 3600) / 60;
            long seconds = totalSecs % 60;
            String timeString = String.format(Locale.ENGLISH,"%02d:%02d", minutes, seconds);
            holder.durationTxtView.setText(timeString);

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
    public Sin getItem(int position){return mDataset.get(position);}
    public String getItemRef(int position){return sinsRefs.get(position);}
    public void removeItem(int position){mDataset.remove(position);}
    public void removeItemRef(int position){sinsRefs.remove(position);}
}