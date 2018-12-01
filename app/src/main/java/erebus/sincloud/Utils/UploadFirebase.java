package erebus.sincloud.Utils;

import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import androidx.annotation.NonNull;

public class UploadFirebase
{
    /**
    * @param localFilename Local object filename
    * @param remotePath Remote path without '/'
    * @param fileExtension File extension
    */
    public void UploadFileFirebase(String localFilename, String remotePath, String fileExtension)
    {
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to "mountains.jpg"
        StorageReference fileRef = storageRef.child(remotePath + "/" + UUID.randomUUID().toString() + fileExtension);

        Uri file = Uri.fromFile(new File(localFilename));
        UploadTask uploadTask = fileRef.putFile(file);

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
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }
}
