package hzhl.net.hlcall.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;

/**
 * 个人设置
 */
@Entity
public class SettingEntity {
    @Id(autoincrement = true)
    private Long id;
    private int missedCallWarn;//未接提醒
    private boolean isAutoAnswerCall;//自动接听
    private boolean isAutoAnswerCallVideo;//视频自动接听
    private boolean isFloat;//悬浮
    private boolean isLockScreen;//锁屏
    private boolean isBoHaoType;//呼叫前确认
    private int boHaoType;//拨号类型，0为sip,1为SIM
    private boolean isAutoStart;//是否开启回声消除
    private int floatWindowParamsX;
    private int floatWindowParamsY;

    public boolean getIsLockScreen() {
        return this.isLockScreen;
    }

    public void setIsLockScreen(boolean isLockScreen) {
        this.isLockScreen = isLockScreen;
    }

    public boolean getIsFloat() {
        return this.isFloat;
    }

    public void setIsFloat(boolean isFloat) {
        this.isFloat = isFloat;
    }

    public boolean getIsAutoAnswerCall() {
        return this.isAutoAnswerCall;
    }

    public void setIsAutoAnswerCall(boolean isAutoAnswerCall) {
        this.isAutoAnswerCall = isAutoAnswerCall;
    }

    public int getMissedCallWarn() {
        return this.missedCallWarn;
    }

    public void setMissedCallWarn(int missedCallWarn) {
        this.missedCallWarn = missedCallWarn;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getBoHaoType() {
        return this.boHaoType;
    }

    public void setBoHaoType(int boHaoType) {
        this.boHaoType = boHaoType;
    }

    public boolean getIsBoHaoType() {
        return this.isBoHaoType;
    }

    public void setIsBoHaoType(boolean isBoHaoType) {
        this.isBoHaoType = isBoHaoType;
    }

    public int getFloatWindowParamsY() {
        return this.floatWindowParamsY;
    }

    public void setFloatWindowParamsY(int floatWindowParamsY) {
        this.floatWindowParamsY = floatWindowParamsY;
    }

    public int getFloatWindowParamsX() {
        return this.floatWindowParamsX;
    }

    public void setFloatWindowParamsX(int floatWindowParamsX) {
        this.floatWindowParamsX = floatWindowParamsX;
    }



    public boolean getIsAutoStart() {
        return this.isAutoStart;
    }

    public void setIsAutoStart(boolean isAutoStart) {
        this.isAutoStart = isAutoStart;
    }

    public boolean getIsAutoAnswerCallVideo() {
        return this.isAutoAnswerCallVideo;
    }

    public void setIsAutoAnswerCallVideo(boolean isAutoAnswerCallVideo) {
        this.isAutoAnswerCallVideo = isAutoAnswerCallVideo;
    }


    @Generated(hash = 1631772215)
    public SettingEntity(Long id, int missedCallWarn, boolean isAutoAnswerCall,
            boolean isAutoAnswerCallVideo, boolean isFloat, boolean isLockScreen,
            boolean isBoHaoType, int boHaoType, boolean isAutoStart, int floatWindowParamsX,
            int floatWindowParamsY) {
        this.id = id;
        this.missedCallWarn = missedCallWarn;
        this.isAutoAnswerCall = isAutoAnswerCall;
        this.isAutoAnswerCallVideo = isAutoAnswerCallVideo;
        this.isFloat = isFloat;
        this.isLockScreen = isLockScreen;
        this.isBoHaoType = isBoHaoType;
        this.boHaoType = boHaoType;
        this.isAutoStart = isAutoStart;
        this.floatWindowParamsX = floatWindowParamsX;
        this.floatWindowParamsY = floatWindowParamsY;
    }

    @Generated(hash = 1082063668)
    public SettingEntity() {
    }

}
