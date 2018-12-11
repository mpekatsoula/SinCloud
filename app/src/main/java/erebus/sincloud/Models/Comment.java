package erebus.sincloud.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

public class Comment
{
    // Sin class
    private String username;
    private String comment;
    private HashMap<String, Object> commentTime;
    private long likes;

    public Comment()
    {
    }
    public Comment(String username, String comment, long likes)
    {
        this.username = username;
        this.comment = comment;
        this.likes = likes;

        commentTime = new HashMap<>();
        commentTime.put("timestamp", ServerValue.TIMESTAMP);
    }

    public String getUsername() {
        return username;
    }
    public String getComment() {
        return comment;
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