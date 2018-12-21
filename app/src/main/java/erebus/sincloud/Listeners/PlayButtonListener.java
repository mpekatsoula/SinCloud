package erebus.sincloud.Listeners;

import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Singletons.SinAudioPlayer;
import erebus.sincloud.UI.SinsMenuAdapter;

public class PlayButtonListener implements View.OnClickListener
{
    private SinsMenuAdapter mAdapter;
    public PlayButtonListener(SinsMenuAdapter mAdapter)
    {
        this.mAdapter = mAdapter;
    }

    @Override
    public void onClick(View v)
    {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
        final int position = viewHolder.getAdapterPosition();
        final Sin sin = mAdapter.getItem(position);
        Button button = v.findViewById(R.id.sin_view_user_play_button);
        SinAudioPlayer.getInstance().playSin(sin.getUrl(), button);
    }
}
