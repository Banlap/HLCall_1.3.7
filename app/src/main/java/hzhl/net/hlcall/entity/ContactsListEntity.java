package hzhl.net.hlcall.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通讯录体类
 * Created by user on 2017/4/5.
 * dana
 */

public class ContactsListEntity implements Parcelable {
    private String name;
    private ArrayList<NumberEntity> numberList;
    private String company;
    private String job;
    private String id;
    private String photoPath;
    private byte[] bytes;
    private String sortLetters;  //显示数据拼音的首字母

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<NumberEntity> getNumberList() {
        return numberList;
    }

    public void setNumberList(ArrayList<NumberEntity> numberList) {
        this.numberList = numberList;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeTypedList(this.numberList);
        dest.writeString(this.company);
        dest.writeString(this.job);
        dest.writeString(this.id);
        dest.writeString(this.photoPath);
        dest.writeByteArray(this.bytes);
        dest.writeString(this.sortLetters);
    }

    public ContactsListEntity() {
    }

    protected ContactsListEntity(Parcel in) {
        this.name = in.readString();
        this.numberList = in.createTypedArrayList(NumberEntity.CREATOR);
        this.company = in.readString();
        this.job = in.readString();
        this.id = in.readString();
        this.photoPath = in.readString();
        this.bytes = in.createByteArray();
        this.sortLetters = in.readString();
    }

    public static final Creator<ContactsListEntity> CREATOR = new Creator<ContactsListEntity>() {
        @Override
        public ContactsListEntity createFromParcel(Parcel source) {
            return new ContactsListEntity(source);
        }

        @Override
        public ContactsListEntity[] newArray(int size) {
            return new ContactsListEntity[size];
        }
    };
}
