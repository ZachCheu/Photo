package io.zirui.nccamera.view.survey;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.surveymonkey.surveymonkeyandroidsdk.SurveyMonkey;
import com.surveymonkey.surveymonkeyandroidsdk.utils.SMError;

import org.json.JSONException;
import org.json.JSONObject;

import io.zirui.nccamera.R;
import io.zirui.nccamera.storage.LocalSPData;

import static android.app.Activity.RESULT_OK;

public class SurveyFragment extends Fragment{

    private static final String SAMPLE_APP = "Camera App";
    private static final int RQ_SM_CODE = 2;
    private static final String SURVEY_HASH = "7QX53V7"; // Should be replaced by real hash!!
    public static final int SURVEY_TRIGGER_NUMBER = 15;
    public static final String SM_ERROR = "smError";

    private SurveyMonkey sdkInstance;

    public static SurveyFragment newInstance(Bundle bundle){
        SurveyFragment surveyFragment = new SurveyFragment();
        return surveyFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sdkInstance = new SurveyMonkey();
        sdkInstance.onStart(getActivity(), SAMPLE_APP, RQ_SM_CODE, SURVEY_HASH);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sdkInstance.startSMFeedbackActivityForResult(getActivity(), RQ_SM_CODE, SURVEY_HASH);
        //sdkInstance.newSMFeedbackFragmentInstance(SURVEY_HASH);
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //sdkInstance.newSMFeedbackFragmentInstance(SURVEY_HASH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RQ_SM_CODE && resultCode == RESULT_OK){
            super.onActivityResult(requestCode, resultCode, intent);
            //String respondent = intent.getStringExtra("smRespondent");
            if(LocalSPData.isOnDate){
                LocalSPData.storeDateTriggerRecord(getContext());
                LocalSPData.isOnDate = false;
                // System.out.println("-----------trigger path: isOnDate");
            }else{
                LocalSPData.storeSurveyRecord(getContext());
                // System.out.println("-----------trigger path: !isOnDate");
            }
            Toast t = Toast.makeText(getContext(), "Thanks for your time!", Toast.LENGTH_LONG);
            t.show();
            getActivity().finish();
        }else{
            Toast t = Toast.makeText(getContext(), getString(R.string.error_prompt), Toast.LENGTH_LONG);
            t.show();
            SMError e = (SMError) intent.getSerializableExtra(SM_ERROR);
            Log.d("SM-ERROR", e.getDescription());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
