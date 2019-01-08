package erebus.sincloud.Utils;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import java.lang.ref.WeakReference;

import erebus.sincloud.R;

public class AudioPlayer extends AsyncTask<String, Void, Boolean>
{
    private MediaPlayer mediaPlayer;
    private WeakReference<Button> button;
    private String TAG = "AudioPlayer";

    public AudioPlayer(MediaPlayer mediaPlayer, Button button)
    {
        this.mediaPlayer = mediaPlayer;
        this.button = new WeakReference<>(button);
    }

    @Override
    protected Boolean doInBackground(String... strings)
    {
        Boolean prepared;
        try
        {
            if(button != null)
            {
                button.get().setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24px);
            }
            mediaPlayer.setDataSource(strings[0]);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer)
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    if(button != null)
                    {
                        button.get().setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
                    }
                }
            });

            mediaPlayer.prepare();
            prepared = true;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
            prepared = false;
        }

        return prepared;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean)
    {
        super.onPostExecute(aBoolean);
        mediaPlayer.start();
    }
}
