package hzhl.net.hlcall.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hzhl.net.hlcall.utils.Sha1Util;

public class Requset {
   private String appid ;
   private String ts ;
   private String nonce ;
   private String signature;
   private String data;
   private String key;


    public Requset(String data) throws NoSuchAlgorithmException {
        this.appid = "HUALUO_DIS";
        this.ts = String.valueOf(System.currentTimeMillis());
        this.nonce = String.valueOf(new Random().nextInt(100));
        this.key = "DJ82*$Scxetyuow3ss";
        String[] strings = new String[]{appid,ts,nonce,key};
        Arrays.sort(strings);
        StringBuilder sb = new StringBuilder();
        for (String s1:strings) {
            sb.append(s1);
        }

        System.out.println("signature = " + sb + "\n");
        this.signature = Sha1Util.getSha1(sb.toString().getBytes());
        setData(data);
    }

    public static Map<String,String> getParams(String data) throws NoSuchAlgorithmException {
        Requset r = new Requset(data);
        Map<String,String> params = new HashMap<>();
        params.put("appid",r.getAppid());
        params.put("ts",r.getTs());
        params.put("nonce",r.getNonce());
        params.put("signature",r.getSignature());
        params.put("data",r.getData());
        return params;
    }


    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        try {
            this.data = URLEncoder.encode( data, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //this.data = data;
    }
}
