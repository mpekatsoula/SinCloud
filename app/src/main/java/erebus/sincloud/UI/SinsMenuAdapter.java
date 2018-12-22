package erebus.sincloud.UI;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    private String TAG = "SinsMenuAdapter";
    private SinMenuAdapterTypes adapterType;
    private View.OnClickListener playButtonViewOnClickListener;
    private View.OnClickListener deleteButtonViewOnClickListener;
    private View.OnClickListener innerConstraintLayoutViewOnClickListener;

    public void setPlayClickListener(View.OnClickListener clickListener)
    {
        playButtonViewOnClickListener = clickListener;
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
        TextView nicknameTxtView;
        ConstraintLayout innerConstraintLayout;
        Button playButton;
        Button deleteButton = null;

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
                deleteButton.setTag(this);
                deleteButton.setOnClickListener(deleteButtonViewOnClickListener);
            }
            else
            {
                titleTxtView = v.findViewById(R.id.sin_view_title);
                likesTxtView = v.findViewById(R.id.sin_view_likes);
                commentsTxtView = v.findViewById(R.id.sin_view_comments);
                dateTxtView = v.findViewById(R.id.sin_view_date);
                playButton = v.findViewById(R.id.sin_view_play_button);
                nicknameTxtView = v.findViewById(R.id.sin_view_nickname);
                innerConstraintLayout = v.findViewById(R.id.sin_view_inner_constraint_layout);
            }

            playButton.setTag(this);
            playButton.setOnClickListener(playButtonViewOnClickListener);
            innerConstraintLayout.setTag(this);
            innerConstraintLayout.setOnClickListener(innerConstraintLayoutViewOnClickListener);
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
            case NOTIFICATIONS:
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
    public Sin getItem(int position){return mDataset.get(position);}
    public String getItemRef(int position){return sinsRefs.get(position);}
    public void removeItem(int position){mDataset.remove(position);}
    public void removeItemRef(int position){sinsRefs.remove(position);}
}