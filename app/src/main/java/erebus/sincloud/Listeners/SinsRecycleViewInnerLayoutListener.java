package erebus.sincloud.Listeners;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import erebus.sincloud.Activities.DisplaySinActivity;
import erebus.sincloud.UI.SinsMenuAdapter;

public class SinsRecycleViewInnerLayoutListener implements View.OnClickListener
{
    private Context context;
    private SinsMenuAdapter adapter;
    public SinsRecycleViewInnerLayoutListener(Context context, SinsMenuAdapter mAdapter)
    {
        this.context = context;
        this.adapter = mAdapter;
    }

    @Override
    public void onClick(View v)
    {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) v.getTag();
        final int pos = viewHolder.getAdapterPosition();

        // Make sure adapter is not empty
        if(adapter.getItemCount() <= 0)
        {
            return;
        }

        Intent intent = new Intent(context, DisplaySinActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("sinRef", adapter.getItemRef(pos));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
