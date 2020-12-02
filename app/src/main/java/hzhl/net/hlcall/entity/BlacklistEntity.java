package hzhl.net.hlcall.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * Created by guang on 2019/8/16.
 */
@Entity
public class BlacklistEntity implements Serializable {
    private static final long serialVersionUID = -179682815073967709L;
    @Id(autoincrement = true)
    private Long id;
    private int count;
    private String user;
    public String getUser() {
        return this.user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public int getCount() {
        return this.count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 418385818)
    public BlacklistEntity(Long id, int count, String user) {
        this.id = id;
        this.count = count;
        this.user = user;
    }
    @Generated(hash = 200903150)
    public BlacklistEntity() {
    }
}
