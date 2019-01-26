package erebus.sincloud.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cn.iwgang.countdownview.CountdownView;
import erebus.sincloud.Helpers.AudioEffect;
import erebus.sincloud.Helpers.ButtonVisibility;
import erebus.sincloud.Helpers.RecordButtonStates;
import erebus.sincloud.R;
import erebus.sincloud.Utils.AudioFilters;
import erebus.sincloud.Utils.UploadFirebase;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

interface setUploadCancelButtonsVisibilityCallback
{
    void setUploadCancelButtonsVisibility(ButtonVisibility visibility);
}

public class RecordSinActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, setUploadCancelButtonsVisibilityCallback
{
    private static final String TAG = "RecordSinActivity";
    private static final String COMPLETED_ONBOARDING = "RecordSinActivity";
    private MediaRecorder audioRecorder;
    private FloatingActionButton recordSinButton;
    private FloatingActionButton pauseRecordingButton;
    private FloatingActionButton cancelRecordingButton;
    private FloatingActionButton uploadRecordingButton;
    private FloatingActionButton playbackRecordingButton;
    private BoomMenuButton filtersMenuButton;
    private RecordButtonStates nextRecordButtonState;
    private CountdownView countdownView;
    private boolean has_permissions = false;
    private String sinFilename;
    private String sinFilenameFinal;
    private int MAX_CHARACTER_LIMIT = 32;
    private final int RECORD_TIME_IN_SEC = 60;
    private final int RECORD_TIME_IN_MS = 1000 * RECORD_TIME_IN_SEC;
    private AnimationDrawable confessionAnimation;
    private MediaPlayer mediaPlayer;
    AudioFilters audioFilters;
    private AudioEffect audioEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sin);

        nextRecordButtonState = RecordButtonStates.START_RECORDING;
        recordSinButton = findViewById(R.id.record_sin_activity_start_stop);
        pauseRecordingButton = findViewById(R.id.record_sin_activity_pause);
        cancelRecordingButton = findViewById(R.id.record_sin_activity_cancel);
        uploadRecordingButton = findViewById(R.id.record_sin_activity_upload);
        playbackRecordingButton = findViewById(R.id.record_sin_activity_play_pause_container);
        countdownView = findViewById(R.id.record_sin_activity_countdown);
        filtersMenuButton = findViewById(R.id.record_sin_activity_filters_button);
        audioEffect = AudioEffect.NONE;

        ImageView backToolbarButton = findViewById(R.id.record_sin_activity_back);
        backToolbarButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        countdownView.updateShow(RECORD_TIME_IN_MS);

        checkPermissions();
        setupRecordingButtons();
        setupCountdown();
        setupFilterButtons();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPreferences.getBoolean(COMPLETED_ONBOARDING, false))
        {
            // This is the first time running the app, let's go to onboarding
            displayOnboarding();
        }
        displayOnboarding();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        ImageView rocketImage = findViewById(R.id.record_sin_activity_animation);
        rocketImage.setBackgroundResource(R.drawable.confession_animation);
        confessionAnimation = (AnimationDrawable) rocketImage.getBackground();
        confessionAnimation.setOneShot(false);

        mediaPlayer = new MediaPlayer();
        audioFilters = new AudioFilters();
    }

    private void setupCountdown()
    {
        // When the countdown finishes, stop recording
        countdownView.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener()
        {
            @Override
            public void onEnd(CountdownView cv)
            {
                pauseStopRecordingAction();
                confessionAnimation.stop();
            }
        });
    }

    private void pauseStopRecordingAction()
    {
        recordSinButton.setImageResource(R.drawable.ic_round_mic_24px);
        pauseRecordingButton.setImageResource(R.drawable.ic_round_pause_24px);
        cancelRecordingButton.setImageResource(R.drawable.ic_round_cancel_24px);
        uploadRecordingButton.setImageResource(R.drawable.ic_round_cloud_upload_24px);
        audioRecorder.stop();
        audioRecorder.reset();
        audioRecorder.release();
        audioRecorder = null;
        setUploadCancelButtonsVisibility(ButtonVisibility.VISIBLE);
        recordSinButton.hide();
        pauseRecordingButton.hide();
        nextRecordButtonState = RecordButtonStates.UPLOAD_CANCEL;
        countdownView.pause();

        if(audioEffect != AudioEffect.NONE)
        {
            applyFilter();
        }
        else
        {
            sinFilenameFinal = sinFilename;
        }
    }

    private void setupRecordingButtons()
    {
        recordSinButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!has_permissions)
                {
                    checkPermissions();
                    return;
                }
                switch (nextRecordButtonState)
                {
                    case START_RECORDING:
                        // Disable the button in case the user clicks it very fast
                        recordSinButton.setClickable(false);
                        confessionAnimation.start();

                        // Don't forget to reset the state of cancel button after the user has uploaded a sin
                        cancelRecordingButton.setClickable(true);
                        uploadRecordingButton.setClickable(true);

                        MediaRecorderReady();
                        Log.d(TAG, "State: recordSinButton START_RECORDING" );
                        recordSinButton.setImageResource(R.drawable.ic_round_stop_24px);
                        try
                        {
                            audioRecorder.prepare();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        audioRecorder.start();
                        nextRecordButtonState = RecordButtonStates.STOP_RECORDING;
                        pauseRecordingButton.show();

                        // Enable the button again
                        recordSinButton.setClickable(true);

                        // Start countdown
                        countdownView.start(RECORD_TIME_IN_MS);
                        break;
                    case STOP_RECORDING:
                        Log.d(TAG, "State:recordSinButton  STOP_RECORDING" );
                    case PAUSE_RECORDING:
                        Log.d(TAG, "State:recordSinButton  PAUSE_RECORDING" );
                        pauseStopRecordingAction();
                        confessionAnimation.stop();
                        break;
                }
            }
        });

        pauseRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (nextRecordButtonState)
                {
                    case STOP_RECORDING:
                        Log.d(TAG, "State: pauseRecordingButton STOP_RECORDING" );
                        audioRecorder.pause();
                        countdownView.stop();
                        confessionAnimation.stop();
                        pauseRecordingButton.setImageResource(R.drawable.ic_round_mic_24px);
                        nextRecordButtonState = RecordButtonStates.PAUSE_RECORDING;
                        break;
                    case PAUSE_RECORDING:
                        Log.d(TAG, "State: pauseRecordingButton PAUSE_RECORDING" );
                        audioRecorder.resume();
                        countdownView.start(countdownView.getRemainTime());
                        confessionAnimation.start();
                        pauseRecordingButton.setImageResource(R.drawable.ic_round_pause_24px);
                        nextRecordButtonState = RecordButtonStates.STOP_RECORDING;
                        break;
                }
            }
        });

        playbackRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    playbackRecordingButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24px);
                }
                else
                {
                    playbackRecordingButton.setImageResource(R.drawable.ic_round_stop_24px);
                    playRecording(v.getContext());
                }
            }
        });

        cancelRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    playbackRecordingButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24px);
                }

                nextRecordButtonState = RecordButtonStates.START_RECORDING;
                recordSinButton.show();
                setUploadCancelButtonsVisibility(ButtonVisibility.INVISIBLE);
                countdownView.updateShow(RECORD_TIME_IN_MS);
                confessionAnimation.stop();
            }
        });

        uploadRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Create an alert dialog to get title
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams")
                View alertLayout = inflater.inflate(R.layout.alert_dialog_sin_name, null);
                final TextView charactersLeft = alertLayout.findViewById(R.id.alert_dialog_characters_left);
                final TextView charactersTotal = alertLayout.findViewById(R.id.alert_dialog_total_characters);
                final String charLimit = "/  " + MAX_CHARACTER_LIMIT;
                charactersTotal.setText(charLimit);
                final TextInputEditText input = alertLayout.findViewById(R.id.alert_dialog_sin_name_text);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_CHARACTER_LIMIT) });

                charactersLeft.setText(String.valueOf(MAX_CHARACTER_LIMIT));
                input.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after)
                    {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {

                    }

                    @Override
                    public void afterTextChanged(Editable s)
                    {
                        charactersLeft.setText(String.valueOf(MAX_CHARACTER_LIMIT - s.length()));
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(RecordSinActivity.this);
                builder.setTitle(getString(R.string.sin_name_promt));
                builder.setView(alertLayout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String sinName = input.getText().toString();
                        if(sinName.length() == 0)
                        {
                            return;
                        }
                        nextRecordButtonState = RecordButtonStates.START_RECORDING;
                        uploadSinToFirebase(sinName);
                        // Reset countdown
                        countdownView.updateShow(RECORD_TIME_IN_MS);
                        confessionAnimation.stop();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void playRecording(Context ctx)
    {
        try
        {
            mediaPlayer.setDataSource(sinFilenameFinal);
        }
        catch (IOException e)
        {
            Toast.makeText(ctx, "Error while playing audio recording", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra)
            {
                Log.d(TAG, "what: "  + what);
                return true;
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                playbackRecordingButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24px);
                mp.stop();
                mp.reset();
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                mp.start();
            }
        });
        mediaPlayer.prepareAsync();
    }

    @Override
    public void setUploadCancelButtonsVisibility(ButtonVisibility visibility)
    {
        switch (visibility)
        {
            case VISIBLE:
                playbackRecordingButton.show();
                cancelRecordingButton.show();
                uploadRecordingButton.show();
                break;
            case INVISIBLE:
                cancelRecordingButton.hide();
                uploadRecordingButton.hide();
                playbackRecordingButton.hide();
                break;
        }
    }

    private void MediaRecorderReady()
    {
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setAudioEncodingBitRate(16);
        audioRecorder.setAudioSamplingRate(44100);

        sinFilename = Objects.requireNonNull(getExternalFilesDir("recordings")).getAbsolutePath();
        sinFilename += "/" + System.currentTimeMillis() + "_sincloud.m4a";
        Log.d(TAG, "Filename for recorded audio: " + sinFilename);
        audioRecorder.setOutputFile(sinFilename);
    }

    private void uploadSinToFirebase(String sinName)
    {
        final UploadFirebase uploadFile = new UploadFirebase();
        long timeRecored = RECORD_TIME_IN_SEC - countdownView.getRemainTime() / 1000;
        uploadFile.UploadSinToFirebase(sinFilenameFinal, sinName, timeRecored, recordSinButton, this);
        cancelRecordingButton.setClickable(false);
        uploadRecordingButton.setClickable(false);
    }

    private void checkPermissions()
    {
        String[] perms = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms))
        {
            has_permissions = true;
        }
        else
        {
            // Do not have permissions, request them now
            int RC_AUDIO_AND_STORAGE = 1;
            EasyPermissions.requestPermissions(this, getString(R.string.audio_and_storage_rationale),
                    RC_AUDIO_AND_STORAGE, perms);
        }
    }

    private void deleteOldFile()
    {
        if(!sinFilenameFinal.equals(sinFilename))
        {
            File file = new File(sinFilenameFinal);
            file.delete();
        }
    }
    private void setupFilterButtons()
    {
        HamButton.Builder noFilterBld = new HamButton.Builder()
                .normalImageRes(R.drawable.ic_baseline_not_interested_24px)
                .normalText("None!")
                .subNormalText("No filter applied!")
                .listener(new OnBMClickListener()
                {
                    @Override
                    public void onBoomButtonClick(int index)
                    {
                        // Remove filter
                        if(audioEffect != AudioEffect.NONE)
                        {
                            deleteOldFile();
                            sinFilenameFinal = sinFilename;
                        }

                        audioEffect = AudioEffect.NONE;
                    }
                });

        HamButton.Builder slowFilterBld = new HamButton.Builder()
                .normalImageRes(R.drawable.ic_baseline_exposure_neg_2_24px)
                .normalText("Slow voice!")
                .subNormalText("Slow down the pitch of your voice!")
                .listener(new OnBMClickListener()
                {
                    @Override
                    public void onBoomButtonClick(int index)
                    {
                        // Remove filter
                        if(audioEffect != AudioEffect.SLOW)
                        {
                            deleteOldFile();
                            audioEffect = AudioEffect.SLOW;
                            applyFilter();
                        }
                        audioEffect = AudioEffect.SLOW;
                    }
                });

        HamButton.Builder fastFilterBld = new HamButton.Builder()
                .normalImageRes(R.drawable.ic_baseline_exposure_plus_2_24px)
                .normalText("Fast voice!")
                .subNormalText("Make your voice sound faster!")
                .listener(new OnBMClickListener()
                {
                    @Override
                    public void onBoomButtonClick(int index)
                    {
                        // Remove filter
                        if(audioEffect != AudioEffect.FAST)
                        {
                            deleteOldFile();
                            audioEffect = AudioEffect.FAST;
                            applyFilter();
                        }
                        audioEffect = AudioEffect.FAST;
                    }
                });

        filtersMenuButton.addBuilder(noFilterBld);
        filtersMenuButton.addBuilder(slowFilterBld);
        filtersMenuButton.addBuilder(fastFilterBld);

    }

    private void applyFilter()
    {
        sinFilenameFinal = audioFilters.changePlaybackSpeed(sinFilename, RecordSinActivity.this, audioEffect);
    }

    private void displayOnboarding()
    {
        TapTargetSequence tapSequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(findViewById(R.id.record_sin_activity_start_stop), "Click to record.", "Click here to start recording your sin. You have 1 minute!")
                        .outerCircleColor(R.color.md_blue_A700)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.95f)            // Specify the alpha amount for the outer circle
                        .titleTextSize(25)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.md_white_1000)      // Specify the color of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .textColor(R.color.md_black_1000)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.md_black_1000)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(54),
                        TapTarget.forView(findViewById(R.id.record_sin_activity_filters_button), "Apply voice filters!", "Change your voice before uploading by selecting a filter!")
                                .outerCircleColor(R.color.md_blue_A700)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.95f)            // Specify the alpha amount for the outer circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.md_white_1000)      // Specify the color of the title text
                                .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                .targetCircleColor(R.color.md_red_400)
                                .textColor(R.color.md_black_1000)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.md_black_1000)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(54),
                        TapTarget.forView(findViewById(R.id.record_sin_activity_upload), "Upload your sin!", "When you are done, click here to upload your sin to the cloud!")
                                .outerCircleColor(R.color.md_blue_A700)      // Specify a color for the outer circle
                                .outerCircleAlpha(0.95f)            // Specify the alpha amount for the outer circle
                                .titleTextSize(25)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.md_white_1000)      // Specify the color of the title text
                                .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                                .targetCircleColor(R.color.md_red_400)
                                .textColor(R.color.md_black_1000)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.md_black_1000)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .icon(getDrawable(R.drawable.ic_round_cloud_upload_24px))
                                .targetRadius(54)
                        );
        tapSequence.start();

        // User has seen OnBoarding, so mark our SharedPreferences
        // flag as completed.
        SharedPreferences.Editor sharedPreferencesEditor;
        sharedPreferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        sharedPreferencesEditor.putBoolean(COMPLETED_ONBOARDING, true);
        sharedPreferencesEditor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms)
    {
        Log.d(TAG, "Permissions grated!");
        has_permissions = true;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms)
    {
        has_permissions = false;
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
        {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (audioRecorder != null)
        {
            audioRecorder.release();
            audioRecorder = null;
        }

        if(mediaPlayer != null)
        {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (audioRecorder != null)
        {
            audioRecorder.stop();
        }

        if(mediaPlayer != null  && mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
        }
    }
}
