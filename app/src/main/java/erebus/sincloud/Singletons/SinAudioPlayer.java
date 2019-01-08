package erebus.sincloud.Singletons;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Button;

import java.lang.ref.WeakReference;

import erebus.sincloud.R;
import erebus.sincloud.Utils.AudioPlayer;

public class SinAudioPlayer
{
    private static SinAudioPlayer playerInstance;
    private MediaPlayer player;
    private AudioPlayer audioPlayer;
    private WeakReference<Button> playingButton;
    private boolean isPaused = false;

    public static SinAudioPlayer getInstance()
    {
        if (playerInstance == null)
        {
            playerInstance = new SinAudioPlayer();
        }
        return playerInstance;
    }

    private SinAudioPlayer()
    {
        //Prevent form the reflection api.
        if (playerInstance != null)
        {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    private void resetPlayerState()
    {
        if(audioPlayer != null)
        {
            audioPlayer.cancel(true);
            audioPlayer = null;
        }
        player.stop();
        player.reset();
        isPaused = false;
    }

    public void playSin(String url, Button button)
    {
        if (player == null)
        {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_SYSTEM);
        }

        if(button != null && playingButton != null && playingButton.get() == button)
        {
            if(isPaused)
            {
                player.start();
                isPaused = false;
                return;
            }
            if(player.isPlaying())
            {
                player.pause();
                isPaused = true;
                return;
            }
        }

        resetPlayerState();

        // Change old button's state
        if(playingButton != null && playingButton.get() != null)
        {
            playingButton.get().setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
        }

        playingButton = new WeakReference<>(button);
        audioPlayer = new AudioPlayer(player, button);
        audioPlayer.execute(url);
    }

    public void stopPlayback()
    {
        if (player == null)
        {
            return;
        }
        resetPlayerState();

        if(playingButton != null)
        {
            if(playingButton.get() != null)
            {
                playingButton.get().setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
            }
            playingButton = null;
        }
    }
}
