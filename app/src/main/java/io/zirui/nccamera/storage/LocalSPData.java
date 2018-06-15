package io.zirui.nccamera.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.zirui.nccamera.utils.RandomStringUtils;

public class LocalSPData {

    public static final int RQ_SM_CODE = 2;
    public static final String SURVEY_HASH = "Q5GPCRQ"; // Should be replaced by real hash!!
    public static final int[] SURVEY_TRIGGER_NUMBERS = new int[]{2, 4, 6};

    // trigger date
    public static final String date = "2018-06-14 20:47:00";
    public static final String[] SURVEY_TRIGGER_DATES = new String[]{"2018-06-14 21:23:00", "2018-06-14 23:23:00"};

    private static final String SP = "share_preference";
    private static final String SP_SURVEY_TIMESTAMP = "survey_timestamp";
    // private static final String SP_SURVEY_DATE_FINISHED = "survey_date_finished";
    private static final String SP_SURVEY_TRIGGER_POINT = "survey_trigger_point";
    private static final String SP_SURVEY_DATE_TRIGGER_POINT = "survey_date_trigger_point";
    private static final String SP_RANDOM_ID = "random_id";
    private static final String SP_START_DATE = "start_date";
    private static final String SP_SESSION = "session_count";

    public static int currentTriggerPoint;
    public static int currentDateTriggerPoint;

    public static boolean isOnDate = false;

    private static SharedPreferences getSharedPreference(@NonNull Context context){
        return context.getApplicationContext().getSharedPreferences(
                SP, Context.MODE_PRIVATE);
    }

    public static int loadAndStoreSession(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        int sessionCount = sp.getInt(SP_SESSION, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SP_SESSION, sessionCount+1);
        editor.apply();
        return sessionCount;
    }

    public static String loadStartDate(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        return sp.getString(SP_START_DATE, null);
    }

    public static void storeStartDate(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_START_DATE, new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date()));
        editor.apply();
    }

    public static String loadRandomID(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        return sp.getString(SP_RANDOM_ID, null);
    }

    public static void storeRandomID(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_RANDOM_ID, new RandomStringUtils().generateString());
        editor.apply();
    }

    public static int loadDateTriggerRecord(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        // return sp.getBoolean(SP_SURVEY_FINISHED, false);
        currentDateTriggerPoint = sp.getInt(SP_SURVEY_DATE_TRIGGER_POINT, 0);
        System.out.println("-----------trigger point: " + currentDateTriggerPoint);
        return currentDateTriggerPoint;
    }

    public static void storeDateTriggerRecord(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_SURVEY_TIMESTAMP, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        editor.putInt(SP_SURVEY_DATE_TRIGGER_POINT, currentDateTriggerPoint + 1);
        editor.apply();
    }

    public static int loadSurveyRecord(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        // return sp.getBoolean(SP_SURVEY_FINISHED, false);
        currentTriggerPoint = sp.getInt(SP_SURVEY_TRIGGER_POINT, 0);
        return currentTriggerPoint;
    }

    public static void storeSurveyRecord(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_SURVEY_TIMESTAMP, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        // editor.putBoolean(SP_SURVEY_FINISHED, true);
        editor.putInt(SP_SURVEY_TRIGGER_POINT, currentTriggerPoint + 1);
        editor.apply();
    }
}
