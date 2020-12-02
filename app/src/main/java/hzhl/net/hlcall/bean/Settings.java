package hzhl.net.hlcall.bean;

public class Settings {
    private String settingName;
    private int imageId;

    public Settings(String settingName, int imageId){
        this.settingName = settingName;
        this.imageId = imageId;
    }

    public String getSettingName() {
        return settingName;
    }


    public int getImageId() {
        return imageId;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
