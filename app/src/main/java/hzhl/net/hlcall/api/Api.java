package hzhl.net.hlcall.api;

import androidx.annotation.NonNull;


import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

import hzhl.net.hlcall.App;
import hzhl.net.hlcall.utils.DataCache;
import io.itit.itf.okhttp.PostBuilder;
import io.itit.itf.okhttp.callback.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static java.lang.System.out;

public class Api {
    //public static String IP = "115.204.65.175:8799";
    public static final String MOVING_IP = "moving_ip";
    public static String IP = new DataCache(App.sContext).getString(MOVING_IP);
    public static String BASE_URL = "http://"+IP+"/atstar/client.php/";
    public static boolean IS_LOG = false;

    /**
     * @param sender       发送者的分机号码
     * @param gt_create_at 过滤条件，消息创建起始时间,格式为 YYYY-mm-dd hh:MM:ss
     * @param lt_create_at 过滤条件，消息创建结束时间,格式为 YYYY-mm-dd hh:MM:ss
     * @param callback     callback
     */
    public static void getMsgList(@NonNull String sender,
                                  String gt_create_at,
                                  String lt_create_at,
                                  Callback callback) {
        PostBuilder builder = newRequest(
                new MsgListRequest(sender,gt_create_at,lt_create_at).toJson(),
                "sipmsg-mms/listmsg",
                callback
        );
        if (builder == null)return;
        builder.build().executeAsync(callback);

    }

    /**
     * @param sender   发送方分机号
     * @param receiver 接收方分机号，为空
     * @param title    标题，最大不超过 100 个汉字
     * @param msg      图片说明，最大不超过 300 个汉字
     * @param files    文件列表
     * @param callback 回调
     */

    public static void upload(@NonNull String sender,
                              String receiver,
                              String title,
                              String msg,
                              List<File> files,
                              Callback callback) {
        PostBuilder builder = newRequest(
                new UpLoadRequest(sender,receiver,title,msg).toJson(),
                "sipmsg-mms/upload",
                callback
        );
        if (builder == null)return;
        if (files != null) for (File file : files) builder.addFile("pic[]", file.getName(), file);
        builder.build().executeAsync(callback);
    }


    public static void download(String uuid,Callback callback){
        PostBuilder builder = newRequest(
                new DownloadRequest(uuid).toJson(),
                "attach-interface/dl",
                callback);
        if (builder == null)return;
        builder.build()
                .executeAsync(callback);
    }


    public static PostBuilder newRequest(String data,String url,Callback callback){
        try {
            Map<String, String> params = Requset.getParams(data);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(BASE_URL).append(url).append("\n");
            for (String s :params.keySet()) {
                stringBuilder.append(s)
                        .append(" = ")
                        .append(params.get(s))
                        .append("\n");
            }
            out.println("Interceptor request:" +stringBuilder.toString());
            //使用代理
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.108", 5555));
            OkHttpClient client = new OkHttpClient().newBuilder()
                    //.proxy(proxy)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        long t1 = System.nanoTime();//请求发起的时间
                        out.println("Interceptor request:" + String.format("发送请求 %s on %s%n%s%n%s",
                                request.url(),
                                chain.connection(),
                                request.headers(),
                                stringBuilder.toString()
                                ));
                        okhttp3.Response response = chain.proceed(request);
                        long t2 = System.nanoTime();//收到响应的时间
                        //这里不能直接使用response.body().string()的方式输出日志
                        //因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一个新的response给应用层处理
                        ResponseBody responseBody = response.peekBody(1024 * 1024);
                        if (IS_LOG)out.println("Interceptor response: "+ responseBody.string());
                        return response;
                    })
                    .build();
            return new PostBuilder(client)
             //       .addHeader("Content-Type","application/json")
                    .url(BASE_URL.trim() + url)
                    .addParams(params);
             //       .body(body);
        }catch (Exception e){
            e.printStackTrace();
            callback.onFailure(null,e,1);
        }
        return null;
    }



    //测试代码
    /*public static void main(String[] args) {
        String s = "";
               *//*getMsgList("8007", "2020-1-7 00:00:00", "2020-1-30 00:00:00", new Callback() {
                    @Override
                    public void onFailure(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response, int id) {
                        try {
                            out.println(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });*//*
        *//*List<File> files = new ArrayList<>();
        File file = new File("E:\\wenyeyang\\icon_hengdianjszb.png");
        files.add(file);
        out.println(file.getName());
        upload("8045", "8046", "8046", "8046", files, new Callback() {
            @Override
            public void onFailure(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response, int id) {

                try {
                    out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*//*

        download("B45C6E92-8E04-C72A-2270-15F7B96AB7B8", new Callback() {
            @Override
            public void onFailure(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response, int id) {
                File file = new File("D:\\QMDownload\\home2.jpg");
                if (!file.exists()) {
                    try {
                        //file.createNewFile();
                        FileOutputStream out = new FileOutputStream(file);
                        out.write(response.body().bytes());
                        out.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }*/


    /*public static void upLoadForOkHttp() throws IOException {

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.108", 5555));
        OkHttpClient client = new OkHttpClient().newBuilder()
                .proxy(proxy)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(RequestBody.create(mediaType, "{\"sender\":\"8045\",\"receiver\":\"8045\",\"title\":\"8046\",\"msg\":\"8046\"}"))
                .addFormDataPart("pics[]","/C:/Users/Administrator/Desktop/home1.jpg",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File("/C:/Users/Administrator/Desktop/home1.jpg")))
                .build();
        //body = RequestBody.create(mediaType, "{\"sender\":\"8045\",\"receiver\":\"8045\",\"title\":\"8046\",\"msg\":\"8046\"}");
        Request request = new Request.Builder()
                .url("http://115.216.110.14:8799/atstar/client.php/sipmsg-mms/upload?signature=a7b2d1feaf573c30984f724ee5d3bfdef2d9ccb8&appid=HUALUO_DIS&ts=1579066576447&nonce=42")
                .method("POST", body)
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        out.println(body.contentType().toString());
        out.println(response.body().string());
    }*/


    /**
     * 日志拦截器
     */
    /*private static final Interceptor mLoggingInterceptor = chain -> {
        Request request = chain.request();
        long t1 = System.nanoTime();//请求发起的时间
        out.println("Interceptor request:" + String.format("发送请求 %s on %s%n%s", request.url(), chain.connection(), request.headers()));
        okhttp3.Response response = chain.proceed(request);
        long t2 = System.nanoTime();//收到响应的时间
        //这里不能直接使用response.body().string()的方式输出日志
        //因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一个新的response给应用层处理
        ResponseBody responseBody = response.peekBody(1024 * 1024);
        out.println("Interceptor response: "+ responseBody.string());
        return response;
    };*/

    public static void setIP(String ip){
        IP = ip;
        new DataCache(App.sContext).putString(MOVING_IP,ip.trim());
        BASE_URL = "http://"+IP+"/atstar/client.php/";
    }
}


