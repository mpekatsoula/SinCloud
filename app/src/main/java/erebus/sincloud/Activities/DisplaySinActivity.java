package erebus.sincloud.Activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import erebus.sincloud.Models.Comment;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Singletons.SinAudioPlayer;
import erebus.sincloud.UI.CommentAdapter;
import rm.com.audiowave.AudioWaveView;


public class DisplaySinActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener
{
    private Sin sin;
    private ImageView playButton;
    private AudioWaveView waveform;
    private TextView toolbarTextView = null;
    private TextView likesTextView = null;
    private TextView commentsTextView = null;
    private EditText chatMessageText = null;
    private ImageView sendMessageButton = null;
    private ImageView likeSinButton = null;
    private String sinRefString;
    private byte[] audioFileRAW;
    final long MAX_DATA_SIZE = 1024 * 1024;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Comment> commentsArray = new ArrayList<>();
    private ArrayList<String> commentsRefs = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private waveAnimation waveAnimationCall;
    private Timer audioTimer;
    private boolean likedStatus = false;
    private long localSinLikes;

    public interface startWaveAnimationCallback
    {
        void startWaveAnimation();
    }

    public class waveAnimation implements startWaveAnimationCallback
    {
        @Override
        public void startWaveAnimation()
        {
            // Needs to run on UI thread
            final int duration =  SinAudioPlayer.getInstance().getMediaPlayerDuration();
            final int amountToUpdate = duration / 100;
            audioTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            waveform.setProgress(SinAudioPlayer.getInstance().getMediaPlayerProgress());
                        }
                    });
                }
            }, 0, amountToUpdate);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_sin);

        // Get sin reference
        sinRefString = getIntent().getStringExtra("sinRef");
        final DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinRefString);
        sinRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                sin = dataSnapshot.getValue(Sin.class);
                if(sin == null)
                {
                    Toast.makeText(getApplicationContext(), "The selected sin was not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                getFirebaseFile();
                setupLikeButton();
                checkUserLike();
                setupCommentInput();
                displayComments();
                toolbarTextView.setText(sin.getTitle());
                likesTextView.setText(String.valueOf(sin.getLikes()));
                localSinLikes = sin.getLikes();
                // Update notify
                if(sin.getUserid().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
                {
                    FirebaseDatabase.getInstance().getReference().child("notify").child("sin").child(sinRefString).setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        playButton = findViewById(R.id.display_sin_activity_play_button);
        waveform = findViewById(R.id.display_sin_activity_waveform);
        toolbarTextView = findViewById(R.id.display_activity_toolbar_text);
        likesTextView = findViewById(R.id.display_sin_activity_likes_textview);
        commentsTextView = findViewById(R.id.display_sin_activity_comment_textview);
        chatMessageText = findViewById(R.id.display_sin_activity_chat_message_text);
        sendMessageButton = findViewById(R.id.activity_display_chat_send_button);
        likeSinButton = findViewById(R.id.display_sin_activity_like_button);
        ImageView backToolbarButton = findViewById(R.id.display_activity_back);
        mSwipeRefreshLayout = findViewById(R.id.display_sin_activity_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        waveAnimationCall = new waveAnimation();

        playButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                handleAudioPlayback();
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
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        SinAudioPlayer.getInstance().stopPlayback();
        waveAnimationCall = null;
        if(audioTimer != null)
        {
            audioTimer.cancel();
            audioTimer = null;
        }
    }

    // Checks if the user has liked the message
    private void checkUserLike()
    {
        // Check if the user has liked the sin before.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("slikes").child(sinRefString).child(user.getUid());
            userRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Object likedStatusObject = dataSnapshot.getValue();
                    if(likedStatusObject != null)
                    {
                        likedStatus = (boolean) likedStatusObject;
                        if(likedStatus)
                        {
                            // Change the color of like button
                            likeSinButton.setBackgroundTintList(ContextCompat.getColorStateList(DisplaySinActivity.this, R.color.colorAccent));
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
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
                    // Change like status
                    likedStatus = !likedStatus;
                    FirebaseDatabase.getInstance().getReference().child("slikes").child(sinRefString).child(user.getUid()).setValue(likedStatus);

                    // Increase/decrease the like counter
                    if(likedStatus)
                    {
                        // Change the color of like button
                        likeSinButton.setBackgroundTintList(ContextCompat.getColorStateList(DisplaySinActivity.this, R.color.colorAccent));
                        localSinLikes++;
                        likesTextView.setText(String.valueOf(localSinLikes));
                    }
                    else
                    {
                        // Change the color of like button
                        likeSinButton.setBackgroundTintList(ContextCompat.getColorStateList(DisplaySinActivity.this, R.color.md_black_1000));
                        localSinLikes--;
                        likesTextView.setText(String.valueOf(localSinLikes));
                    }
                }
            }
        });
    }

    private void setupCommentInput()
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
                    return;
                }
                chatMessageText.setText("");

                // Store a reference in the user's database that he made a comment
                FirebaseDatabase.getInstance().getReference().child("users").child(userUid).child("scomments").child(sinRefString).setValue(true);

                // Update notifications db
                if(sin != null && !sin.getUserid().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
                {
                    FirebaseDatabase.getInstance().getReference().child("notify").child("sin").child(sinRefString).setValue(true);
                }

                final DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("comments").child(sinRefString).push();
                Comment comment = new Comment(userUid, message, 0, commentsRef.getKey());
                commentsRef.setValue(comment);
                getComments();
            }
        });
    }

    private void loadWaveform()
    {
        waveform.setRawData(audioFileRAW);
        waveform.setTouchable(false);
        waveform.setProgress(0);
    }

    private void handleAudioPlayback()
    {
        if(audioTimer != null)
        {
            audioTimer.cancel();
            audioTimer = null;
        }
        audioTimer = new Timer();
        SinAudioPlayer.getInstance().playSin(sin.getUrl(), playButton, waveAnimationCall);
    }

    private void displayComments()
    {
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
                commentsTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));

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
        if(sin != null)
        {
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(sin.getUrl());
            storageRef.getBytes(MAX_DATA_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>()
            {
                @Override
                public void onSuccess(byte[] bytes)
                {
                    audioFileRAW = bytes;
                    loadWaveform();
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception exception)
                {
                }
            });
        }
    }

    @Override
    public void onRefresh()
    {
        getComments();
    }
}
