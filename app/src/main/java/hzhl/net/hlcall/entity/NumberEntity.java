package hzhl.net.hlcall.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 联系人号码
 */
public class NumberEntity implements Parcelable {
    private String id;//data表_id
    private String number;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NumberEntity() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.number);
        dest.writeString(this.type);
    }

    protected NumberEntity(Parcel in) {
        this.id = in.readString();
        this.number = in.readString();
        this.type = in.readString();
    }

    public static final Creator<NumberEntity> CREATOR = new Creator<NumberEntity>() {
        @Override
        public NumberEntity createFromParcel(Parcel source) {
            return new NumberEntity(source);
        }

        @Override
        public NumberEntity[] newArray(int size) {
            return new NumberEntity[size];
        }
    };
}
