package hzhl.net.hlcall.entity;

import java.io.Serializable;

/**
 * Created by guang on 2019/8/9.
 */

public class SipEntity implements Serializable {
    private static final long serialVersionUID = -2597990032224999428L;
    private String id;
    private String user;
    private String code;
    private String shouQuanUser;
    private boolean isRegist;
    private String type;

    public boolean isRegist() {
        return isRegist;
    }

    public void setRegist(boolean regist) {
        isRegist = regist;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getShouQuanUser() {
        return shouQuanUser;
    }

    public void setShouQuanUser(String shouQuanUser) {
        this.shouQuanUser = shouQuanUser;
    }


    private boolean isLogin;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
