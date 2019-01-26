package erebus.sincloud.Listeners;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Singletons.SinAudioPlayer;
import erebus.sincloud.UI.SinsMenuAdapter;

public class PlayButtonListener implements View.OnClickListener
{
    private SinsMenuAdapter mAdapter;
    private SinMenuAdapterTypes adapterType;
    public PlayButtonListener(SinsMenuAdapter mAdapter, SinMenuAdapterTypes adapterType)
    {
        this.mAdapter = mAdapter;
        this.adapterType = adapterType;
    }

    @Override
    public void onClick(View v)
    {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
        final int position = viewHolder.getAdapterPosition();
        final Sin sin = mAdapter.getItem(position);
        ImageView button;
        if(adapterType == SinMenuAdapterTypes.USER_SETTINGS)
        {
            button = v.findViewById(R.id.sin_view_user_play_button);
        }
        else
        {
            button = v.findViewById(R.id.sin_view_play_button);
        }
        SinAudioPlayer.getInstance().playSin(sin.getUrl(), button, null);
    }
}
