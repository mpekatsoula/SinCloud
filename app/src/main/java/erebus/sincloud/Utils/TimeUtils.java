package erebus.sincloud.Utils;

import java.util.TimeZone;

public class TimeUtils
{
    private final long ONE_DAY_IN_MS = 1000*60*60*24;

    public long ConvertServerTimeToLocal(long timeInMS)
    {
        long messageLocalTime = timeInMS + TimeZone.getDefault().getRawOffset();
        return (System.currentTimeMillis() - messageLocalTime) / ONE_DAY_IN_MS;
    }

}
