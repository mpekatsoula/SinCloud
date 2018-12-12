package erebus.sincloud.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class Comment
{
    // Sin class
    private String username;
    private String comment;
    private String key;
    private HashMap<String, Object> commentTime;
    private long likes;

    public Comment()
    {
    }
    public Comment(String username, String comment, long likes, String key)
    {
        this.username = username;
        this.comment = comment;
        this.likes = likes;
        this.key = key;

        commentTime = new HashMap<>();
        commentTime.put("timestamp", ServerValue.TIMESTAMP);
    }

    public String getUsername() {
        return username;
    }
    public String getComment() {
        return comment;
    }
    public String getKey() {
        return key;
    }
    public HashMap<String, Object> getCommentTime() {
        return commentTime;
    }
    public long getLikes() {
        return likes;
    }

    @Exclude
    public long getMessageTimeLong()
    {
        if(getCommentTime() != null)
        {
            return (long) commentTime.get("timestamp");
        }
        return 1;
    }
}