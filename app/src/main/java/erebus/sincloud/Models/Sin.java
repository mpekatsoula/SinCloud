package erebus.sincloud.Models;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class Sin
{
    // Sin class
    private String url;
    private String title;
    private long time;
    private long likes;
    private long comments;
    private HashMap<String, Object> sinTime;

    public Sin()
    {
    }
    public Sin(String url, String title, long time, long likes, long comments)
    {
        this.url = url;
        this.title = title;
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

}