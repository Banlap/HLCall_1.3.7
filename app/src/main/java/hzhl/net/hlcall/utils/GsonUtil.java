package hzhl.net.hlcall.utils;

import com.google.gson.Gson;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;

import io.itit.itf.okhttp.Response;

public class GsonUtil {

    public static <T> T fromJson(String json,Class<T> t){
        try {
            return new Gson().fromJson(json,t);
        }catch (Exception e){
            e.printStackTrace();
            e.addSuppressed(new Throwable(json));
            CrashReport.postCatchedException(e);
        }
        return null;
    }

    public static <T> T fromJson(Response response, Class<T> t){
        try {
            return fromJson(response.body().string(),t);
        } catch (IOException e) {
            e.printStackTrace();
            CrashReport.postCatchedException(e);
        }
        return null;
    }
}
