package erebus.sincloud.Utils;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import androidx.annotation.NonNull;
import erebus.sincloud.Activities.RecordSinActivity;
import erebus.sincloud.Helpers.ButtonVisibility;
import erebus.sincloud.Models.Sin;

public class UploadFirebase
{
    private static final String TAG = "UploadFirebase";

    /**
     * @param localFilename Local object filename
     * @param timeRecored Time in seconds for the recorded sin
     * @param sinName Name of the sin given by the user
     * @param recordSinButton
     * @param recordSinActivity
     */
    public void UploadSinToFirebase(String localFilename, final String sinName, final long timeRecored, final FloatingActionButton recordSinButton, final RecordSinActivity recordSinActivity)
    {
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to sin
        final StorageReference fileRef = storageRef.child("/sins" + UUID.randomUUID().toString() + ".3gp");

        Log.d(TAG, "Local filename: " + localFilename);
        Uri file = Uri.fromFile(new File(localFilename));
        final UploadTask uploadTask = fileRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {

                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if(user != null)
                            {
                                String sinId = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("sins").push().getKey();
                                if(sinId != null)
                                {
                                    DatabaseReference sinRefUser = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("sins").child(sinId);
                                    sinRefUser.setValue(true);

                                    // Store sin to sin database
                                    Sin newSin = new Sin(uri.toString(), sinName, user.getUid(), timeRecored, 0, 0);
                                    DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinId);
                                    sinRef.setValue(newSin);
                                }
                            }

                            recordSinActivity.setUploadCancelButtonsVisibility(ButtonVisibility.INVISIBLE);
                            recordSinButton.show();
                        }
                    });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
            {
                double progress = (100.f * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>()
        {

            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot)
            {
                Log.d(TAG, "Upload is paused");
            }
        });
    }
}
