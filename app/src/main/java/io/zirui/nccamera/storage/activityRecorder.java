package io.zirui.nccamera.storage;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class activityRecorder {
    private boolean onCamera;
    private boolean onImage;
    private boolean inBackground;
    private boolean homeOrSwitch;
    private int[] activityCounter;
    private long activityStart;
    private int ignoreThree;

    public activityRecorder(boolean onCamera, boolean onImage, boolean inBackground, boolean homeOrSwitch){
        this.onCamera = onCamera;
        this.onImage = onImage;
        this.inBackground = inBackground;
        // 0 = outside app, 1 = gallery, 2 = image, 3 = camera
        activityCounter = new int[4];
        ignoreThree = 3;
    }

    public boolean isOnCamera() {
        return onCamera;
    }

    public void setOnCamera(boolean onCamera) {
        this.onCamera = onCamera;
    }

    public boolean isOnImage() {
        return onImage;
    }

    public void setOnImage(boolean onImage) {
        this.onImage = onImage;
    }

    public boolean isInBackground() {
        return inBackground;
    }

    public void setInBackground(boolean inBackground) {
        this.inBackground = inBackground;
    }

    public boolean isHomeOrSwitch() {
        return homeOrSwitch;
    }

    public void setHomeOrSwitch(boolean homeOrSwitch) {
        this.homeOrSwitch = homeOrSwitch;
    }

    public void record(Context context, DatabaseReference dataSession){
        //leaving gallery to camera and images so record gallery
        inBackground = isAppIsInBackground(context);
        Log.e("background", ""+inBackground);
        Log.e("background3", ""+homeOrSwitch);
        if(toRecord()){
        DatabaseReference createSession = dataSession.child("Gallery" + activityCounter[1]++);
        createSession.setValue(getDuration());
    }
    activityStart = System.currentTimeMillis();
}
    public void postRecord(DatabaseReference dataSession){
        //coming back from either camera and images
        DatabaseReference createSession;
        Log.e("background2", ""+inBackground);
        if(!toRecord()){
            inBackground = false;
            createSession = dataSession.child("OutsideApp" + activityCounter[0]++);
        } else {
            if(isOnCamera()){
                createSession = dataSession.child("Camera" + activityCounter[3]++);
            } else {
                createSession = dataSession.child("Image" + activityCounter[2]++);
            }
        }
        onCamera = false;
        createSession.setValue(getDuration());
        activityStart = System.currentTimeMillis();
    }

    public long getDuration(){
        return System.currentTimeMillis() - activityStart;
    }
    public long getActivityStart() {
        return activityStart;
    }

    public void setActivityStart(long activityStart) {
        this.activityStart = activityStart;
    }


    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }
        return isInBackground;
    }

    private boolean toRecord(){
        return !inBackground && !homeOrSwitch;
    }
}
