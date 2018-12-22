package erebus.sincloud.Utils;

import com.google.firebase.database.FirebaseDatabase;
import com.instabug.library.Instabug;

public class Initialization extends android.app.Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        new Instabug.Builder(this, "1bcb1f1e67c8ae11f8af76272b2d6f9a")
                .build();
    }
}
