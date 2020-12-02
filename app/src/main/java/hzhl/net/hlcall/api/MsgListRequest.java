package hzhl.net.hlcall.api;

import java.util.List;

public class MsgListRequest {
    private String eq_sender;
    private String gt_create_at;
    private String lt_create_at;

    public MsgListRequest(String eq_sender, String gt_create_at, String lt_create_at) {
        this.eq_sender = eq_sender;
        this.gt_create_at = gt_create_at;
        this.lt_create_at = lt_create_at;
    }


    public String toJson() {
        return "{"
                + "\"eq_sender\":\""
                + eq_sender + '\"'
                + ",\"gt_create_at\":\""
                + gt_create_at + '\"'
                + ",\"lt_create_at\":\""
                + lt_create_at + '\"'
                + "}";
    }
}
