package erebus.sincloud.Models;

public class Comment
{
    // Sin class
    private String username;
    private String comment;
    private long time;
    private long likes;

    public Comment()
    {
    }
    public Comment(String username, String comment, long time, long likes)
    {
        this.username = username;
        this.comment = comment;
        this.time = time;
        this.likes = likes;
    }

    public String getUsername() {
        return username;
    }
    public String getComment() {
        return comment;
    }
    public long getTime() {
        return time;
    }
    public long getLikes() {
        return likes;
    }
}