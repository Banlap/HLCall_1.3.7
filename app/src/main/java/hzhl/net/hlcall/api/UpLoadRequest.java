package hzhl.net.hlcall.api;

public class UpLoadRequest {
    private String sender;
    private String receiver;
    private String title;
    private String content;

    public UpLoadRequest(String sender, String receiver, String title, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.title = title;
        this.content = content;
    }

    public String toJson() {
        return "{"
                + "\"sender\":\""
                + sender + '\"'
                + ",\"receiver\":\""
                + receiver + '\"'
                + ",\"title\":\""
                + title + '\"'
                + ",\"content\":\""
                + content + '\"'
                + "}";

    }
}
