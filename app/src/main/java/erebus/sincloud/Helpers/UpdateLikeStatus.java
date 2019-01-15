package erebus.sincloud.Helpers;

import android.content.Context;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import erebus.sincloud.R;
import erebus.sincloud.UI.SinsMenuAdapter;

public class UpdateLikeStatus
{
    private SinsMenuAdapter adapter;
    private LinearLayoutManager manager;
    private Context context;
    private SinMenuAdapterTypes adapterType;

    public UpdateLikeStatus(SinsMenuAdapter mAdapter, LinearLayoutManager manager, Context context, SinMenuAdapterTypes adapterType)
    {
        this.adapter = mAdapter;
        this.manager = manager;
        this.context = context;
        this.adapterType = adapterType;
    }

    public void Update()
    {
        for (int i = manager.findFirstVisibleItemPosition(); i < manager.findLastVisibleItemPosition(); ++i)
        {
            // Check if the user has liked the sin before.
            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser())
                            .getUid()).child("likes").child(adapter.getItemRef(i));

            final int iFinal = i;
            userRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    Object likedStatusObject = dataSnapshot.getValue();
                    if(likedStatusObject != null)
                    {
                        boolean likedStatus = (boolean) likedStatusObject;
                        if(likedStatus)
                        {
                            View elementView = manager.getChildAt(iFinal);
                            if(elementView == null)
                            {
                                return;
                            }

                            // Change the color of like button
                            if(adapterType == SinMenuAdapterTypes.USER_SETTINGS)
                            {
                                elementView.findViewById(R.id.sin_view_user_like_image).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccent));
                            }
                            else
                            {
                                elementView.findViewById(R.id.sin_view_like_image).setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorAccent));
                            }
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
}
