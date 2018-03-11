package io.zirui.nccamera.storage;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.zirui.nccamera.utils.RandomStringUtils;

public class LocalSPData {

    public static final String SAMPLE_APP = "Camera App";
    public static final int RQ_SM_CODE = 2;
    public static final String SURVEY_HASH = "Q5GPCRQ"; // Should be replaced by real hash!!
    public static final int SURVEY_TRIGGER_NUMBER = 3;
    public static final String SM_ERROR = "smError";

    public static final String SP = "share_preference";
    public static final String SP_SURVEY_TIMESTAMP = "survey_timestamp";
    public static final String SP_SURVEY_FINISHED = "survey_finished";
    public static final String SP_RANDOM_ID = "random_id";

    private static SharedPreferences getSharedPreference(@NonNull Context context){
        return context.getApplicationContext().getSharedPreferences(
                SP, Context.MODE_PRIVATE);
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

    public static boolean loadSurveyRecord(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        return sp.getBoolean(SP_SURVEY_FINISHED, false);
    }

    public static void storeSurveyRecord(@NonNull Context context){
        SharedPreferences sp = getSharedPreference(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_SURVEY_TIMESTAMP, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        editor.putBoolean(SP_SURVEY_FINISHED, true);
        editor.apply();
    }
}
