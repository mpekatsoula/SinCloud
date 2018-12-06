package erebus.sincloud.UI;

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

public class SinsMenuAdapter extends RecyclerView.Adapter<SinsMenuAdapter.ViewHolder>
{
    private ArrayList<Sin> mDataset;
    private String TAG = "SinsMenuAdapter";

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

        ViewHolder(View v)
        {
            super(v);
            titleTxtView = v.findViewById(R.id.sin_view_title);
            likesTxtView = v.findViewById(R.id.sin_view_likes);
            commentsTxtView = v.findViewById(R.id.sin_view_comments);
            timeTxtView = v.findViewById(R.id.sin_view_time);
            playButton = v.findViewById(R.id.sin_view_play_button);
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

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Log.d(TAG, "onBindViewHolder() position: " + position);

        Sin sinInfo = mDataset.get(position);

        Log.d(TAG, "Position: " + position + " title: " + sinInfo.getTitle());
        holder.titleTxtView.setText(sinInfo.getTitle());
        holder.commentsTxtView.setText(String.valueOf(sinInfo.getComments()));
        holder.likesTxtView.setText(String.valueOf(sinInfo.getLikes()));
        holder.timeTxtView.setText(String.valueOf(sinInfo.getTime()));
    }

    @Override
    public int getItemCount()
    {
        return mDataset.size();
    }
}