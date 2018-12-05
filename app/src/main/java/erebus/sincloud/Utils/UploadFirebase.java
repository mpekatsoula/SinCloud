package erebus.sincloud.Utils;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
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

public class UploadFirebase
{
    private static final String TAG = "UploadFirebase";

    /**
     * @param localFilename Local object filename
     * @param remotePath Remote path without '/'
     * @param fileExtension File extension
     * @param databaseRef If not null, create an entry with the file URL in the specified database path.
     * @param recordSinButton
     * @param recordSinActivity
     */
    public void UploadSinToFirebase(String localFilename, String remotePath, String fileExtension, final DatabaseReference databaseRef, final FloatingActionButton recordSinButton, final ProgressBar uploadProgressBar, final RecordSinActivity recordSinActivity)
    {
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to "mountains.jpg"
        final StorageReference fileRef = storageRef.child(remotePath + "/" + UUID.randomUUID().toString() + fileExtension);

        Log.d(TAG, "Local filename: " + localFilename);
        Uri file = Uri.fromFile(new File(localFilename));
        final UploadTask uploadTask = fileRef.putFile(file);

        uploadProgressBar.setVisibility(View.VISIBLE);
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
                if(databaseRef != null)
                {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            Log.d(TAG, "File Uri: " + uri.toString());
                            Log.d(TAG, "Database ref: " + databaseRef.toString());
                            databaseRef.setValue(uri.toString());
                            recordSinActivity.setUploadCancelButtonsVisibility(ButtonVisibility.INVISIBLE);
                            recordSinButton.show();
                            uploadProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
            {
                double progress = (100.f * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d(TAG, "Upload is " + progress + "% done");
                int currentprogress = (int) progress;
                uploadProgressBar.setProgress(currentprogress);
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
