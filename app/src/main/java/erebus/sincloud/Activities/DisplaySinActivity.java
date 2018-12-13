package erebus.sincloud.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import erebus.sincloud.Models.Comment;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.UI.CommentAdapter;
import erebus.sincloud.Utils.AudioPlayer;
import rm.com.audiowave.AudioWaveView;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class DisplaySinActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener
{
    private String TAG = "DisplaySinActivity";
    private Sin sin;
    private Button playButton;
    private AudioWaveView waveform;
    private MediaPlayer mediaPlayer = null;
    private AudioPlayer audioPlayer = null;
    private TextView toolbarTextView = null;
    private TextView likesTextView = null;
    private TextView commentsTextView = null;
    private EditText chatMessageText = null;
    private Button sendMessageButton = null;
    private Button likeSinButton = null;
    private String sinRefString;
    private byte[] audioFileRAW;
    final long MAX_DATA_SIZE = 1024 * 1024;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Comment> commentsArray = new ArrayList<>();
    private ArrayList<String> commentsRefs = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sin);

        playButton = findViewById(R.id.display_sin_activity_play_button);
        waveform = findViewById(R.id.display_sin_activity_waveform);
        toolbarTextView = findViewById(R.id.display_activity_toolbar_text);
        likesTextView = findViewById(R.id.display_sin_activity_likes_textview);
        commentsTextView = findViewById(R.id.display_sin_activity_comment_textview);
        chatMessageText = findViewById(R.id.display_sin_activity_chat_message_text);
        sendMessageButton = findViewById(R.id.activity_display_chat_send_button);
        likeSinButton = findViewById(R.id.display_sin_activity_like_button);
        Button backToolbarButton = findViewById(R.id.display_activity_back);
        mSwipeRefreshLayout = findViewById(R.id.display_sin_activity_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Get sin reference and create database reference
        sinRefString = getIntent().getStringExtra("sinRef");
        final DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinRefString);
        sinRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                sin = dataSnapshot.getValue(Sin.class);
                getFirebaseFile();
                toolbarTextView.setText(sin.getTitle());
                likesTextView.setText(String.valueOf(sin.getLikes()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                playAudio();
            }
        });
        backToolbarButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        setupLikeButton();
        setupChatInput();
        displayComments();
    }

    private void setupLikeButton()
    {
        likeSinButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Check if the user has liked the sin before.
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null)
                {
                    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("likes").child(sinRefString);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            Object likedStatusObject = dataSnapshot.getValue();
                            boolean likedStatus = false;
                            if(likedStatusObject != null)
                            {
                                likedStatus = (boolean) likedStatusObject;
                            }
                            final DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinRefString).child("likes");
                            userRef.setValue(!likedStatus);
                            // Increase/decrease the like counter
                            if(!likedStatus)
                            {
                                sinRef.runTransaction(new Transaction.Handler()
                                {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData)
                                    {
                                        mutableData.setValue(Integer.parseInt(mutableData.getValue().toString()) + 1);
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot)
                                    {

                                    }
                                });
                            }
                            else
                            {
                                sinRef.runTransaction(new Transaction.Handler()
                                {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData)
                                    {
                                        mutableData.setValue(Integer.parseInt(mutableData.getValue().toString()) - 1);
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot)
                                    {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }
            }
        });
    }

    private void setupChatInput()
    {
        chatMessageText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Enable button only when the user has typed something
                if (s.length() == 0)
                {
                    sendMessageButton.setEnabled(false);
                }
                else
                {
                    sendMessageButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String message;
                String userUid;
                if(FirebaseAuth.getInstance().getCurrentUser() != null)
                {
                    userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
                else
                {
                    // Log error and exit
                    chatMessageText.setText("");
                    return;
                }

                try
                {
                    message = chatMessageText.getText().toString();
                }
                catch (Exception e)
                {
                    Log.e(TAG, e.getMessage());
                    return;
                }
                chatMessageText.setText("");

                final DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("comments").child(sinRefString).push();
                Comment comment = new Comment(userUid, message, 0, commentsRef.getKey());
                commentsRef.setValue(comment);
            }
        });
    }

    private void loadWaveform()
    {
        waveform.setRawData(audioFileRAW);
        waveform.setTouchable(false);
    }

    private void playAudio()
    {
        if(mediaPlayer == null)
        {
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
        audioPlayer = new AudioPlayer(mediaPlayer);
        audioPlayer.execute(sin.getUrl());

        // Needs to run on UI thread
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                Timer audioTimer = new Timer();
                final int duration = mediaPlayer.getDuration();
                final int amoungToupdate = duration / 100;
                audioTimer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run()
                            {
                                float audioProgress = 100.f * mediaPlayer.getCurrentPosition() / (float)mediaPlayer.getDuration();
                                waveform.setProgress(audioProgress);
                            }
                        });
                    }
                }, 0, amoungToupdate);
            }
        });
    }

    private void displayComments()
    {
        Log.d(TAG, "displayChatMessages");

        RecyclerView mRecyclerView = findViewById(R.id.display_sin_activity_recycleview);
        mAdapter = new CommentAdapter(commentsArray, commentsRefs, sinRefString);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        getComments();
    }

    private void getComments()
    {
        final DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("comments").child(sinRefString);
        Query commentsQuery = commentsRef.orderByChild("likes");
        commentsQuery.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                commentsArray.clear();
                commentsRefs.clear();
                for(DataSnapshot data : dataSnapshot.getChildren())
                {
                    commentsArray.add(data.getValue(Comment.class));
                    commentsRefs.add(data.getKey());
                }
                Collections.reverse(commentsArray);
                Collections.reverse(commentsRefs);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
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

    @Override
    public void onRefresh()
    {
        getComments();
    }
}
