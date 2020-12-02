package hzhl.net.hlcall.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by guang on 2019/8/19.
 */
@Entity
public class SipProfileLoginEntity {
    @Id(autoincrement = true)
    private Long id;
    private String uriString;
    public String getUriString() {
        return this.uriString;
    }
    public void setUriString(String uriString) {
        this.uriString = uriString;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 1783531550)
    public SipProfileLoginEntity(Long id, String uriString) {
        this.id = id;
        this.uriString = uriString;
    }
    @Generated(hash = 1930603099)
    public SipProfileLoginEntity() {
    }
}
