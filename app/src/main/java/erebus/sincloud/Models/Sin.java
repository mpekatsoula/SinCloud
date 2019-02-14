package erebus.sincloud.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;

import androidx.annotation.Keep;

@Keep
public class Sin implements Serializable
{
    // Sin class
    public String url;
    public String title;
    public String userid;
    public long time;
    public long likes;
    public long comments;
    public HashMap<String, Object> sinTime;

    public Sin()
    {
    }
    public Sin(String url, String title, String userid, long time, long likes, long comments)
    {
        this.url = url;
        this.title = title;
        this.userid = userid;
        this.time = time;
        this.likes = likes;
        this.comments = comments;


        sinTime = new HashMap<>();
        sinTime.put("timestamp", ServerValue.TIMESTAMP);
    }

    public String getUrl() {
        return url;
    }
    public String getTitle() {
        return title;
    }
    public String getUserid() {
        return userid;
    }
    public long getTime() {
        return time;
    }
    public long getLikes() {
        return likes;
    }
    public long getComments() {
        return comments;
    }
    public HashMap<String, Object> getsinTime() {
        return sinTime;
    }

    @Exclude
    public long getMessageTimeLong()
    {
        if(getsinTime() != null)
        {
            return (long) sinTime.get("timestamp");
        }
        return 1;
    }
}