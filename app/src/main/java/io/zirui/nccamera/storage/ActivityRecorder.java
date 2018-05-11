package io.zirui.nccamera.storage;

import com.google.firebase.database.DatabaseReference;

public class ActivityRecorder {
    public static boolean onCamera = false;
    public static boolean onImage = false;
    //0 = background , 1 = gallery, 2 = image, 3 = camera
    public static int[] activityCounter = new int[4];
    public static long activityStart;
    public static int ignoreThree = 3;

    public static void record(DatabaseReference dataSession){
        if(ignoreThree-- <= 0){
            DatabaseReference createSession = dataSession.child("Gallery" + activityCounter[1]++);
            createSession.setValue(getDuration());
            activityStart = System.currentTimeMillis();
        }
    }
    public static void postRecord(DatabaseReference dataSession){
        //coming back from either camera and images
        if(ignoreThree-- <= 0){
            DatabaseReference createSession;
            if (onCamera) {
                createSession = dataSession.child("Camera" + activityCounter[3]++);
            } else if(onImage){
                createSession = dataSession.child("Image" + activityCounter[2]++);
            } else {
                createSession = dataSession.child("Background" + activityCounter[0] ++);
            }
            onCamera = false;
            onImage = false;
            createSession.setValue(getDuration());
        }
        activityStart = System.currentTimeMillis();
    }

    public static long getDuration(){
        return System.currentTimeMillis() - activityStart;
    }
    public static long getActivityStart() {
        return activityStart;
    }


//    private boolean isAppIsInBackground(Context context) {
//        boolean isInBackground = true;
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
//            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                for (String activeProcess : processInfo.pkgList) {
//                    if (activeProcess.equals(context.getPackageName())) {
//                        isInBackground = false;
//                    }
//                }
//            }
//        }
//        return isInBackground;
//    }
}
