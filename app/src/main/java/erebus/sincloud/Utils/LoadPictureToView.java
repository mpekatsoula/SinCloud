package erebus.sincloud.Utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;


public class LoadPictureToView
{
    public void GetAndLoadProfilePictureToView(final Context context, final ImageView imageView)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Object photoURLObj = dataSnapshot.child("photoURL").getValue();
                    if(photoURLObj != null)
                    {
                        LoadProfilePictureToView(context, imageView, photoURLObj.toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }

    public void LoadProfilePictureToView(final Context context, final ImageView imageView, final String photoURL)
    {
        // Check if context is valid
        if(context == null)
        {
            return;
        }

        try
        {
            Glide.with(context).load(photoURL).into(imageView);
        }
        catch (Exception e)
        {
            // Do nothing
        }
    }
}
