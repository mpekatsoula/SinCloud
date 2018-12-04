package erebus.sincloud.Utils;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import android.net.Uri;
import android.widget.ImageView;


public class LoadPictureToView
{
    private static final String TAG = "GetProfilePicture";

    public void LoadProfilePictureToView(Context context, ImageView imageView)
    {
        Uri photoUrl = null;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            for (UserInfo profile : user.getProviderData())
            {
                Log.d(TAG, profile.getProviderId());
                // check if the provider id matches "facebook.com"
                if (profile.getProviderId().equals("facebook.com"))
                {
                    String facebookUserId = profile.getUid();
                    photoUrl = Uri.parse("https://graph.facebook.com/" + facebookUserId + "/picture?height=500");
                }
                else if (profile.getProviderId().equals("google.com"))
                {
                    photoUrl = user.getPhotoUrl();
                }
                // For now handle only one profile
                break;
            }
            Log.w(TAG, "Loading profile pic: " + photoUrl);
            Glide.with(context).load(photoUrl).into(imageView);
        }
        else
        {
            Log.w(TAG, "Firebase user is null");
        }

    }
}
