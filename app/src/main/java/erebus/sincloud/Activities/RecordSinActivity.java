package erebus.sincloud.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cn.iwgang.countdownview.CountdownView;
import erebus.sincloud.Helpers.ButtonVisibility;
import erebus.sincloud.Helpers.RecordButtonStates;
import erebus.sincloud.R;
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
    private MediaRecorder audioRecorder;
    private FloatingActionButton recordSinButton;
    private FloatingActionButton pauseRecordingButton;
    private FloatingActionButton cancelRecordingButton;
    private FloatingActionButton uploadRecordingButton;
    private ProgressBar uploadProgressBar;
    private RecordButtonStates nextRecordButtonState;
    private CountdownView countdownView;
    private Button backToolbarButton;
    private boolean has_permissions = false;
    private String sinFilename;
    private int MAX_CHARACTER_LIMIT = 128;
    private final int RECORD_TIME_IN_SEC = 3;
    private final int RECORD_TIME_IN_MS = 1000 * RECORD_TIME_IN_SEC;

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
        uploadProgressBar = findViewById(R.id.record_sin_activity_progress_bar);
        countdownView = findViewById(R.id.record_sin_activity_countdown);
        backToolbarButton = findViewById(R.id.record_sin_activity_back);

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
                        pauseRecordingButton.setImageResource(R.drawable.ic_round_mic_24px);
                        nextRecordButtonState = RecordButtonStates.PAUSE_RECORDING;
                        break;
                    case PAUSE_RECORDING:
                        Log.d(TAG, "State: pauseRecordingButton PAUSE_RECORDING" );
                        audioRecorder.resume();
                        countdownView.start(countdownView.getRemainTime());
                        pauseRecordingButton.setImageResource(R.drawable.ic_round_pause_24px);
                        nextRecordButtonState = RecordButtonStates.STOP_RECORDING;
                        break;
                }
            }
        });

        cancelRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                nextRecordButtonState = RecordButtonStates.START_RECORDING;
                recordSinButton.show();
                setUploadCancelButtonsVisibility(ButtonVisibility.INVISIBLE);
                countdownView.updateShow(RECORD_TIME_IN_MS);
            }
        });

        uploadRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Set up the input
                final EditText input = new EditText(RecordSinActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_CHARACTER_LIMIT) });

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
//                        TextView tv = findViewById(R.id.yourTextViewId);
//                        tv.setText(String.valueOf(MAX_CHARACTER_LIMIT - s.length()));
                    }
                });

                // Create an alert dialog to get title
                AlertDialog.Builder builder = new AlertDialog.Builder(RecordSinActivity.this);
                builder.setTitle("What is your sin");
                builder.setView(input);
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

    @Override
    public void setUploadCancelButtonsVisibility(ButtonVisibility visibility)
    {
        switch (visibility)
        {
            case VISIBLE:
                cancelRecordingButton.show();
                uploadRecordingButton.show();
                break;
            case INVISIBLE:
                cancelRecordingButton.hide();
                uploadRecordingButton.hide();
                break;
        }
    }

    private void MediaRecorderReady()
    {
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        sinFilename = Objects.requireNonNull(getExternalFilesDir("recordings")).getAbsolutePath();
        sinFilename += "/" + System.currentTimeMillis() + "_sincloud.3gp";
        Log.d(TAG, "Filename for recorded audio: " + sinFilename);
        audioRecorder.setOutputFile(sinFilename);
    }

    private void uploadSinToFirebase(String sinName)
    {
        final UploadFirebase uploadFile = new UploadFirebase();
        long timeRecored = RECORD_TIME_IN_SEC - countdownView.getRemainTime() / 1000;
        uploadFile.UploadSinToFirebase(sinFilename, sinName, timeRecored, recordSinButton, uploadProgressBar, this);
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
    }
}
