package hzhl.net.hlcall.entity;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
public class IntercomEntity implements Serializable {
    private static final long serialVersionUID = 331365418101101477L;
    @Id(autoincrement = true)//设置自增长
    private Long id;
    private Date createTime;
    private String name;
    @Convert(columnType = String.class, converter = ContactsEntity_Converter.class)
    private List<ContactsEntity> contacts;
    public List<ContactsEntity> getContacts() {
        return this.contacts;
    }
    public void setContacts(List<ContactsEntity> contacts) {
        this.contacts = contacts;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    @Generated(hash = 643702529)
    public IntercomEntity(Long id, Date createTime, String name,
            List<ContactsEntity> contacts) {
        this.id = id;
        this.createTime = createTime;
        this.name = name;
        this.contacts = contacts;
    }
    @Generated(hash = 1112653175)
    public IntercomEntity() {
    }



}
