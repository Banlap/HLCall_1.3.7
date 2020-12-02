package hzhl.net.hlcall.api;

public class DownloadRequest {
    private String uuid;

    public DownloadRequest(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String toJson() {
        return "{"
                + "\"uuid\":\""
                + uuid + '\"'
                + "}";

    }
}
