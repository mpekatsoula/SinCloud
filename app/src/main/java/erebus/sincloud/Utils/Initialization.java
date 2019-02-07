package erebus.sincloud.Utils;

import com.google.firebase.database.FirebaseDatabase;

public class Initialization extends android.app.Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
    }
}
