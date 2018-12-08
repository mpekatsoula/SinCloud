package erebus.sincloud.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import rm.com.audiowave.AudioWaveView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;

public class DisplaySinActivity extends AppCompatActivity
{
    private String TAG = "DisplaySinActivity";
    private Sin sin;
    private Button playButton;
    private AudioWaveView waveform;
    private byte[] audioFileRAW;
    final long MAX_DATA_SIZE = 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sin);

        playButton = findViewById(R.id.display_sin_activity_play_button);
        waveform = findViewById(R.id.display_sin_activity_waveform);

        // Get sin reference and create database reference
        String sinRefString = getIntent().getStringExtra("sinRef");
        final DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinRefString);

        sinRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                sin = dataSnapshot.getValue(Sin.class);
                getFirebaseFile();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
    private void loadWaveform()
    {
        waveform.setRawData(audioFileRAW);
    }
    private void playAudio()
    {

    }
    private void loadComments()
    {

    }
    private void LoadComments()
    {

    }
    private void getFirebaseFile()
    {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(sin.getUrl());
        Log.d(TAG, "loading url: " + sin.getUrl());
        storageRef.getBytes(MAX_DATA_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>()
        {
            @Override
            public void onSuccess(byte[] bytes)
            {
                Log.d(TAG, "getFirebaseFile success");
                audioFileRAW = bytes;
                loadWaveform();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                Log.d(TAG, "getFirebaseFile failure");
            }
        });
    }

}
