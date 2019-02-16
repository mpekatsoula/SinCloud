package erebus.sincloud.Singletons;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;

import erebus.sincloud.Activities.DisplaySinActivity;
import erebus.sincloud.R;

public class SinAudioPlayer
{
    private static SinAudioPlayer playerInstance;
    private MediaPlayer player;
    private WeakReference<ImageView> playingButton;
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
        player.stop();
        player.reset();
        isPaused = false;
        if(playingButton != null && playingButton.get() != null)
        {
            playingButton.get().setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
        }
    }

    public float getMediaPlayerProgress()
    {
        float progress = 0.f;
        if(player != null)
        {
            progress = 100.f * player.getCurrentPosition() / (float) player.getDuration();
            if (progress > 99.f)
            {
                progress = 99.f;
            }
            else if (progress < 1.f)
            {
                progress = 1.f;
            }
        }

        return progress;
    }

    public int getMediaPlayerDuration()
    {
        if(player != null)
        {
            return player.getDuration();
        }

        return 1;
    }

    public void playSin(String url, ImageView button, final DisplaySinActivity.waveAnimation waveAnimationCallback)
    {
        if (player == null)
        {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_SYSTEM);
            player.setVolume(1, 1);
        }

        if(playingButton != null && playingButton.get() != null && playingButton.get() == button)
        {
            // If the player is paused, resume playback
            if(isPaused)
            {
                player.start();
                isPaused = false;
                playingButton.get().setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24px);
                return;
            }

            // If the player is playing, pause playback
            if(player.isPlaying())
            {
                player.pause();
                isPaused = true;
                playingButton.get().setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
                return;
            }
        }

        // Change old button's state
        if(playingButton != null && playingButton.get() != null && playingButton.get() != button)
        {
            playingButton.get().setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24px);
        }

        resetPlayerState();
        playingButton = new WeakReference<>(button);

        try
        {
            player.setDataSource(url);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // When the player is ready start playback
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer)
            {
                mediaPlayer.start();
                if(waveAnimationCallback != null)
                {
                    waveAnimationCallback.startWaveAnimation();
                }
            }
        });
        player.prepareAsync();

        // Change the button's image
        playingButton.get().setBackgroundResource(R.drawable.ic_baseline_pause_circle_outline_24px);

        // When the player is completed, stop and reset it. Also change the button back
        // to the default image
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                resetPlayerState();
            }
        });
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
