package hzhl.net.hlcall.entity;

import android.net.sip.SipProfile;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * Created by guang on 2019/8/16.
 */
@Entity
public class SipProfileEntity implements Serializable {
    private static final long serialVersionUID = 3442308301171500951L;
    @Id(autoincrement = true)
    private Long id;
    private String uriString;
    private String domain;
    private String user;
    private String code;
    private String shouQuanUser;
    private boolean isRegist;
    private boolean isChoose;
    private String type;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getIsRegist() {
        return this.isRegist;
    }

    public void setIsRegist(boolean isRegist) {
        this.isRegist = isRegist;
    }

    public String getShouQuanUser() {
        return this.shouQuanUser;
    }

    public void setShouQuanUser(String shouQuanUser) {
        this.shouQuanUser = shouQuanUser;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUriString() {
        return this.uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public boolean getIsChoose() {
        return this.isChoose;
    }

    public void setIsChoose(boolean isChoose) {
        this.isChoose = isChoose;
    }

    @Generated(hash = 1609162808)
    public SipProfileEntity(Long id, String uriString, String domain, String user,
            String code, String shouQuanUser, boolean isRegist, boolean isChoose, String type) {
        this.id = id;
        this.uriString = uriString;
        this.domain = domain;
        this.user = user;
        this.code = code;
        this.shouQuanUser = shouQuanUser;
        this.isRegist = isRegist;
        this.isChoose = isChoose;
        this.type = type;
    }

    @Generated(hash = 1853677754)
    public SipProfileEntity() {
    }
}
