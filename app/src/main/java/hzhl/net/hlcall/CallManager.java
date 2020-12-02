package hzhl.net.hlcall;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.tools.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hzhl.net.hlcall.call.BandwidthManager;
import hzhl.net.hlcall.utils.MyLog;

public class CallManager {
    private static CallManager mInstance;

    public BandwidthManager mBandwidthManager;

    private CallManager(){
        mBandwidthManager = new BandwidthManager();
    }

    public static CallManager getInstance(){
        if(mInstance == null){
            synchronized (CallManager.class){
                if(mInstance == null) {
                    mInstance = new CallManager();
                }
            }
        }
        return mInstance;
    }

    public void switchCamera() {
        Core core = LinphoneService.getCore();
        try {
            String currentDevice = core.getVideoDevice();
            String newDevice = currentDevice;
            //wenyeyang
            String[] devices = core.getVideoDevicesList();
            core.getVideoDevice();
            StringBuffer stringBuffer = new StringBuffer();
            for (String s:devices
            ) {
                MyLog.d("devices w" + s);
                stringBuffer.append(s);
            }
            //Toast.makeText(LinphoneService.getInstance(), stringBuffer, Toast.LENGTH_SHORT).show();

/*
            for(int i = 0;i<devices.length;i++){
                String d = devices[i];
                if(d.equals(currentDevice)){
                    continue;
                }else {
                    String lowerD = d.toLowerCase();
                    if(lowerD.contains("front") || lowerD.contains("back")){
                        newDevice = d;
                        break;
                    }
                }
            }
*/
            List<String> list = Arrays.asList(devices);
            int i = list.indexOf(currentDevice);
            int b = 0;
            for (int j = i+1; j != i; j++) {
                if (j>= list.size())j=0;
                b++;
                if (b>list.size())break;;
                String d = list.get(j);
                if (d.toLowerCase().contains("static"))continue;
                newDevice = d;
                break;
            }


            if(newDevice.equals(currentDevice)){
                return;
            }

            core.setVideoDevice(newDevice);
            Call call = core.getCurrentCall();
            if (call == null) {
                Log.w("[Call Manager] Trying to switch camera while not in call");
                return;
            }
            call.update(null);
        } catch (ArithmeticException ae) {
            Log.e("[Call Manager] [Video] Cannot switch camera: no camera");
        }

//        Core core = LinphoneService.getCore();
//        try {
//            String currentDevice = core.getVideoDevice();
//            String[] devices = core.getVideoDevicesList();
//            int index = 0;
//            for (String d : devices) {
//                if (d.equals(currentDevice)) {
//                    break;
//                }
//                index++;
//            }
//
//            String newDevice;
//            if (index == 1) newDevice = devices[0];
//            else if (devices.length > 1) newDevice = devices[1];
//            else newDevice = devices[index];
//            core.setVideoDevice(newDevice);
//
//            Call call = core.getCurrentCall();
//            if (call == null) {
//                Log.w("[Call Manager] Trying to switch camera while not in call");
//                return;
//            }
//            call.update(null);
//        } catch (ArithmeticException ae) {
//            Log.e("[Call Manager] [Video] Cannot switch camera: no camera");
//        }
    }

    public void resetCameraFromPreferences() {
        Core core = LinphoneService.getCore();
        if (core == null) return;

        boolean useFrontCam = true;
        String firstDevice = null;

        StringBuilder stringBuffer = new StringBuilder();

        /*
        for (String camera : core.getVideoDevicesList()) {
            stringBuffer.append("摄像头:").append(camera).append("\n");
        }
        */
        for (String camera : core.getVideoDevicesList()) {

            if (firstDevice == null) {
                firstDevice = camera;
            }

            if (camera.toLowerCase().contains("static"))continue;

            if (useFrontCam) {
                if (camera.toLowerCase().contains("front")) {
                    core.setVideoDevice(camera);
                    //wenyeyang
                    Call call = core.getCurrentCall();
                    if (call == null) {
                        Log.w("[Call Manager] Trying to switch camera while not in call");
                        return;
                    }
                    call.update(null);
                    //
                    return;
                }
            }
        }

        core.setVideoDevice(firstDevice);
        //wenyeyang
        Call call = core.getCurrentCall();
        if (call == null) {
            Log.w("[Call Manager] Trying to switch camera while not in call");
            return;
        }
        call.update(null);
    }

    public void addVideo() {
        Core core = LinphoneService.getCore();

        Call call = core.getCurrentCall();
        if (call.getState() == Call.State.End || call.getState() == Call.State.Released) return;
        if (!call.getCurrentParams().videoEnabled()) {
            enableCamera(call, true);
            reInviteWithVideo();
        }
    }

    public void removeVideo() {
        Core core = LinphoneService.getCore();
        Call call = core.getCurrentCall();
        CallParams params = core.createCallParams(call);
        params.enableVideo(false);
        //call.acceptUpdate(params);
        //wenyeyang
        call.update(params);
    }

    private boolean reInviteWithVideo() {
        Core core = LinphoneService.getCore();
        Call call = core.getCurrentCall();
        if (call == null) {
            Log.e("[Call Manager] Trying to add video while not in call");
            return false;
        }
        if (call.getRemoteParams().lowBandwidthEnabled()) {
            Log.e("[Call Manager] Remote has low bandwidth, won't be able to do video");
            return false;
        }

        CallParams params = core.createCallParams(call);

        if (params.videoEnabled()) return false;

        // Check if video possible regarding bandwidth limitations
        mBandwidthManager.updateWithProfileSettings(params);

        // Abort if not enough bandwidth...
        if (!params.videoEnabled()) {
            return false;
        }

        // Not yet in video call: try to re-invite with video
        call.update(params);
        return true;
    }

    private void enableCamera(Call call, boolean enable) {
        if (call != null) {
            call.enableCamera(enable);
        }
    }

    public void acceptCallVideo(boolean accept){
        Core core = LinphoneService.getCore();
        Call call = core.getCurrentCall();
        if (call == null) {
            return;
        }

        CallParams params = core.createCallParams(call);
        if (accept) {
            params.enableVideo(true);
            core.enableVideoCapture(true);
            core.enableVideoDisplay(true);
        }else {
            params.enableVideo(false);
            core.enableVideoCapture(false);
            core.enableVideoDisplay(false);
        }

        call.acceptUpdate(params);
        //wenyeyang
        //call.update(params);
    }


}
