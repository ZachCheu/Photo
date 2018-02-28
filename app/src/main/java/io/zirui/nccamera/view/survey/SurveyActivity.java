package io.zirui.nccamera.view.survey;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import io.zirui.nccamera.view.base.SingleFragmentActivity;

public class SurveyActivity extends SingleFragmentActivity {

    @NonNull
    @Override
    protected Fragment newFragment() {
        return SurveyFragment.newInstance(getIntent().getExtras());
    }
}
