package io.zirui.nccamera.storage;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalSPData {

    public static final String SAMPLE_APP = "Camera App";
    public static final int RQ_SM_CODE = 2;
    public static final String SURVEY_HASH = "7QX53V7"; // Should be replaced by real hash!!
    public static final int SURVEY_TRIGGER_NUMBER = 20;
    public static final String SM_ERROR = "smError";

    public static final String SP_SURVEY = "survey";
    public static final String SP_SURVEY_TIMESTAMP = "survey_timestamp";
    public static final String SP_SURVEY_FINISHED = "survey_finished";

    public static boolean loadSurveyRecord(@NonNull Context context){
        SharedPreferences surveySP = context.getApplicationContext().getSharedPreferences(
                SP_SURVEY, Context.MODE_PRIVATE);
        return surveySP.getBoolean(SP_SURVEY_FINISHED, false);
    }

    public static void storeSurveyRecord(@NonNull Context context){
        SharedPreferences surveySP = context.getApplicationContext().getSharedPreferences(
                SP_SURVEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = surveySP.edit();
        editor.putString(SP_SURVEY_TIMESTAMP, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        editor.putBoolean(SP_SURVEY_FINISHED, true);
        editor.apply();
    }
}
