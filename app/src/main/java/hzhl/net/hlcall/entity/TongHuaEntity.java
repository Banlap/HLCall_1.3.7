package hzhl.net.hlcall.entity;

/**
 * Created by guang on 2019/8/9.
 */

public class TongHuaEntity {
    private String id;
    private String number;
    private String name;
    private String date;//通话日期
    private String duration;;//时长
    private String type;//类型
    private String time;//通话时间
    private String dayString;//今天，昨天，前天
    private boolean isChoose;
    private boolean isSHow;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSHow() {
        return isSHow;
    }

    public void setSHow(boolean SHow) {
        isSHow = SHow;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDayString() {
        return dayString;
    }

    public void setDayString(String dayString) {
        this.dayString = dayString;
    }

    @Override
    public String toString() {
        return "TongHuaEntity{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", date='" + date + '\'' +
                ", duration='" + duration + '\'' +
                ", type='" + type + '\'' +
                ", time='" + time + '\'' +
                ", dayString='" + dayString + '\'' +
                ", isChoose=" + isChoose +
                ", isSHow=" + isSHow +
                '}';
    }
}
