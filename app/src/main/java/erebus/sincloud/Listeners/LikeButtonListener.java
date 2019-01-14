package erebus.sincloud.Listeners;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Activities.DisplaySinActivity;
import erebus.sincloud.Helpers.SinMenuAdapterTypes;
import erebus.sincloud.Models.Sin;
import erebus.sincloud.R;
import erebus.sincloud.Singletons.SinAudioPlayer;
import erebus.sincloud.UI.SinsMenuAdapter;

public class LikeButtonListener implements View.OnClickListener
{
    private SinsMenuAdapter mAdapter;
    private SinMenuAdapterTypes adapterType;
    public LikeButtonListener(SinsMenuAdapter mAdapter, SinMenuAdapterTypes adapterType)
    {
        this.mAdapter = mAdapter;
        this.adapterType = adapterType;
    }

    @Override
    public void onClick(final View v)
    {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
        final int position = viewHolder.getAdapterPosition();
        final Sin sin = mAdapter.getItem(position);
        final String sinRefString = mAdapter.getItemRef(position);
        final ImageView button;
        if(adapterType == SinMenuAdapterTypes.USER_SETTINGS)
        {
            button = v.findViewById(R.id.sin_view_user_like_image);
        }
        else
        {
            button = v.findViewById(R.id.sin_view_like_image);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(user).getUid()).child("likes").child(sinRefString);
        final DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinRefString).child("likes");

        boolean likedStatus = !mAdapter.getItemLiked(position);
        mAdapter.setItemLiked(position, likedStatus);
        userRef.setValue(likedStatus);
        // Increase/decrease the like counter
        if(likedStatus)
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

            // Change the color of like button
            button.setBackgroundTintList(ContextCompat.getColorStateList(v.getContext(), R.color.colorAccent));
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

            // Change the color of like button
            button.setBackgroundTintList(ContextCompat.getColorStateList(v.getContext(), R.color.md_black_1000));
        }
    }
}
