package erebus.sincloud.Activities;

import android.Manifest;
import android.media.MediaRecorder;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import erebus.sincloud.Helpers.ButtonVisibility;
import erebus.sincloud.R;
import erebus.sincloud.Helpers.RecordButtonStates;
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
    private boolean has_permissions = false;
    private String sinFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sin);

        nextRecordButtonState = RecordButtonStates.START_RECORDING;
        recordSinButton = findViewById(R.id.record_start_stop);
        pauseRecordingButton = findViewById(R.id.record_pause);
        cancelRecordingButton = findViewById(R.id.record_cancel);
        uploadRecordingButton = findViewById(R.id.record_upload);
        uploadProgressBar = findViewById(R.id.record_activity_progress_bar);

        checkPermissions();
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
                        // Disable the button in cate the user clicks it very fast
                        recordSinButton.setClickable(false);

                        // Don't forget to reset the state of cancel button after the user has uploaded a sin
                        cancelRecordingButton.setClickable(true);

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
                        break;
                    case STOP_RECORDING:
                        Log.d(TAG, "State:recordSinButton  STOP_RECORDING" );
                    case PAUSE_RECORDING:
                        Log.d(TAG, "State:recordSinButton  PAUSE_RECORDING" );
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
                        pauseRecordingButton.setImageResource(R.drawable.ic_round_mic_24px);
                        nextRecordButtonState = RecordButtonStates.PAUSE_RECORDING;
                        break;
                    case PAUSE_RECORDING:
                        Log.d(TAG, "State: pauseRecordingButton PAUSE_RECORDING" );
                        audioRecorder.resume();
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
            }
        });

        uploadRecordingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                nextRecordButtonState = RecordButtonStates.START_RECORDING;
                uploadSinToFirebase();
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
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        sinFilename = Objects.requireNonNull(getExternalFilesDir("recordings")).getAbsolutePath();
        sinFilename += "/" + System.currentTimeMillis() + "_sincloud.3gp";
        Log.d(TAG, "Filename for recorded audio: " + sinFilename);
        audioRecorder.setOutputFile(sinFilename);
    }

    private void uploadSinToFirebase()
    {
        final UploadFirebase uploadFile = new UploadFirebase();
        long timeRecored = 22;
        uploadFile.UploadSinToFirebase(sinFilename, timeRecored, recordSinButton, uploadProgressBar, this);
        cancelRecordingButton.setClickable(false);
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
    public void onStop() {
        super.onStop();
        if (audioRecorder != null)
        {
            audioRecorder.release();
            audioRecorder = null;
        }
    }
}
