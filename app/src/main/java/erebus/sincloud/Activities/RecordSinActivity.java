package erebus.sincloud.Activities;

import android.media.MediaRecorder;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

import erebus.sincloud.Helpers.ButtonVisibility;
import erebus.sincloud.R;
import erebus.sincloud.Helpers.RecordButtonStates;

public class RecordSinActivity extends AppCompatActivity
{
    private MediaRecorder audioRecorder;
    private FloatingActionButton recordSinButton;
    private FloatingActionButton pauseRecordingButton;
    private FloatingActionButton cancelRecordingButton;
    private FloatingActionButton uploadRecordingButton;
    private RecordButtonStates nextRecordButtonState;

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

        recordSinButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (nextRecordButtonState)
                {
                    case START_RECORDING:
                        MediaRecorderReady();
                        Log.d("RecordSinActivity", "State: recordSinButton START_RECORDING" );
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
                        break;
                    case STOP_RECORDING:
                        Log.d("RecordSinActivity", "State:recordSinButton  STOP_RECORDING" );
                    case PAUSE_RECORDING:
                        Log.d("RecordSinActivity", "State:recordSinButton  PAUSE_RECORDING" );
                        recordSinButton.setImageResource(R.drawable.ic_round_mic_24px);
                        pauseRecordingButton.setImageResource(R.drawable.ic_round_pause_24px);
                        cancelRecordingButton.setImageResource(R.drawable.ic_round_pause_24px);
                        uploadRecordingButton.setImageResource(R.drawable.ic_round_pause_24px);
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
                        Log.d("RecordSinActivity", "State: pauseRecordingButton STOP_RECORDING" );
                        audioRecorder.pause();
                        pauseRecordingButton.setImageResource(R.drawable.ic_round_mic_24px);
                        nextRecordButtonState = RecordButtonStates.PAUSE_RECORDING;
                        break;
                    case PAUSE_RECORDING:
                        Log.d("RecordSinActivity", "State: pauseRecordingButton PAUSE_RECORDING" );
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
                recordSinButton.show();
                setUploadCancelButtonsVisibility(ButtonVisibility.INVISIBLE);
            }
        });
    }

    private void setUploadCancelButtonsVisibility(ButtonVisibility visibility)
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
        audioRecorder.setOutputFile(Environment.getExternalStorageDirectory() + File.separator
                + Environment.DIRECTORY_DCIM + File.separator + "thesaurus");
    }

}
