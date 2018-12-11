package erebus.sincloud.Utils;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

public class AudioPlayer extends AsyncTask<String, Void, Boolean>
{
    private MediaPlayer mediaPlayer;
    private String TAG = "AudioPlayer";

    public AudioPlayer(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer = mediaPlayer;
    }

    @Override
    protected Boolean doInBackground(String... strings)
    {
        Boolean prepared;
        try
        {
            mediaPlayer.setDataSource(strings[0]);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer)
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
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
