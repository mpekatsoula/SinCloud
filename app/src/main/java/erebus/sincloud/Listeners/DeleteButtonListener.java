package erebus.sincloud.Listeners;

import android.content.DialogInterface;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.UI.SinsMenuAdapter;

public class DeleteButtonListener implements View.OnClickListener
{
    private SinsMenuAdapter mAdapter;
    public DeleteButtonListener(SinsMenuAdapter mAdapter)
    {
        this.mAdapter = mAdapter;
    }

    @Override
    public void onClick(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Are you sure you want to delete this sin?");
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();

        final int pos = viewHolder.getAdapterPosition();
        final String sinRefStr = mAdapter.getItemRef(pos);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference sinUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(user).getUid()).child("sins").child(sinRefStr);
                sinUserRef.removeValue();
                DatabaseReference sinRef = FirebaseDatabase.getInstance().getReference().child("sins").child(sinRefStr);
                sinRef.removeValue();
                mAdapter.removeItemRef(pos);
                mAdapter.removeItem(pos);
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
