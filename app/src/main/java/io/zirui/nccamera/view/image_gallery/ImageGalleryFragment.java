package io.zirui.nccamera.view.image_gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.surveymonkey.surveymonkeyandroidsdk.SurveyMonkey;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.zirui.nccamera.R;
import io.zirui.nccamera.listener.ActivityMonitorService;
import io.zirui.nccamera.listener.RecyclerItemClickListener;
import io.zirui.nccamera.model.Shot;
import io.zirui.nccamera.storage.LocalSPData;
import io.zirui.nccamera.storage.ShotDeletor;
import io.zirui.nccamera.storage.ShotLoader;
import io.zirui.nccamera.storage.ShotSaver;
import io.zirui.nccamera.storage.ShotSharer;
import io.zirui.nccamera.utils.DateUtils;
import io.zirui.nccamera.utils.ModelUtils;
import io.zirui.nccamera.view.image_viewpager.ImageViewPagerActivity;
import io.zirui.nccamera.view.image_viewpager.ImageViewPagerFragment;

import static android.app.Activity.RESULT_OK;

public class ImageGalleryFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Shot>> {

    public static final int REQ_CODE_IMAGE_DETAIL_EDIT = 101;
    public static final int MATRIX_NUMBER = 3;
    private static Context mainActivityContext;
    private static Location lastLocation;

    private ImageGalleryAdapter adapter;

    private SurveyMonkey sdkInstance;

    private boolean isMultiSelect = false;
    private ActionMode mActionMode;
    private Menu context_menu;

    private ArrayList<Shot> multiselect_list = new ArrayList<>();
    private RecyclerItemClickListener recyclerItemClickListener;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    @NonNull
    public static ImageGalleryFragment newInstance(Context context, Location location){
        lastLocation = location;
        mainActivityContext = context;
        return new ImageGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(R.id.loader_id_media_store_data1, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, view);
        recyclerItemClickListener = new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent i = new Intent(getContext(), ActivityMonitorService.class);
                i.putExtra("SmartLocation", lastLocation);
                getContext().startService(i);
                if (isMultiSelect)
                    multi_select(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    multiselect_list = new ArrayList<>();
                    isMultiSelect = true;

                    if (mActionMode == null) {

                        mActionMode = ((Activity) getContext()).startActionMode(mActionModeCallBack);
                    }
                }
                multi_select(position);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), MATRIX_NUMBER));
        recyclerView.addItemDecoration(new ImageGalleryDecoration(getResources().getDimensionPixelSize((R.dimen.spacing_xsmall))));
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnItemTouchListener(recyclerItemClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_IMAGE_DETAIL_EDIT && resultCode == RESULT_OK) {
            if(data.getBooleanExtra(ImageViewPagerActivity.KEY_SHOT_DELETE, false)){
                ShotSaver shotSaver = ShotSaver.getInstance(getContext());
                shotSaver.handleBigCameraPhoto();
            }
        }
    }

    @Override
    public Loader<List<Shot>> onCreateLoader(int id, Bundle args) {
        return new ShotLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Shot>> loader, List<Shot> data) {
        adapter =
                new ImageGalleryAdapter(getContext(), data, multiselect_list, new ImageGalleryAdapter.OnClickImageListener() {
                    @Override
                    public void onClick(int position, List<Shot> data) {
                        Intent intent = new Intent(getContext(), ImageViewPagerActivity.class);
                        intent.putExtra(ImageViewPagerFragment.EXTRA_INITIAL_POS, position);
                        intent.putExtra(ImageViewPagerFragment.EXTRA_IMAGES, ModelUtils.toString(data, new TypeToken<List<Shot>>(){}));
                        startActivityForResult(intent, ImageGalleryFragment.REQ_CODE_IMAGE_DETAIL_EDIT);
                    }
                });
        recyclerView.setAdapter(adapter);
        //&& !LocalSPData.loadSurveyRecord(getContext())
        int surveyTriggerPoint = LocalSPData.loadSurveyRecord(getContext());
        int surveyDateTriggerPoint = LocalSPData.loadDateTriggerRecord(getContext());
        int surveyTriggerNumber = surveyTriggerPoint < LocalSPData.SURVEY_TRIGGER_NUMBERS.length ? LocalSPData.SURVEY_TRIGGER_NUMBERS[surveyTriggerPoint] : Integer.MAX_VALUE;
        String surveyDateTriggerDate = surveyDateTriggerPoint < LocalSPData.SURVEY_TRIGGER_DATES.length ? LocalSPData.SURVEY_TRIGGER_DATES[surveyDateTriggerPoint] : null;
        if (adapter.data.size() > surveyTriggerNumber){
            String id = LocalSPData.loadRandomID(getContext());
            Map<String, String> dict = new HashMap<>();
            dict.put("n", id);
            sdkInstance = new SurveyMonkey();
            //sdkInstance.onStart(getActivity(), LocalSPData.SAMPLE_APP, LocalSPData.RQ_SM_CODE, LocalSPData.SURVEY_HASH, new JSONObject(dict));
            sdkInstance.startSMFeedbackActivityForResult(getActivity(), LocalSPData.RQ_SM_CODE, LocalSPData.SURVEY_HASH, new JSONObject(dict));
        }else if(DateUtils.compareDates(surveyDateTriggerDate)){
            // System.out.println("-----------trigger date: " + surveyDateTriggerDate);
            LocalSPData.isOnDate = true;
            String id = LocalSPData.loadRandomID(getContext());
            Map<String, String> dict = new HashMap<>();
            dict.put("n", id);
            sdkInstance = new SurveyMonkey();
            //sdkInstance.onStart(getActivity(), LocalSPData.SAMPLE_APP, LocalSPData.RQ_SM_CODE, LocalSPData.SURVEY_HASH, new JSONObject(dict));
            sdkInstance.startSMFeedbackActivityForResult(getActivity(), LocalSPData.RQ_SM_CODE, LocalSPData.SURVEY_HASH, new JSONObject(dict));
        }

    }

    @Override
    public void onLoaderReset(Loader<List<Shot>> loader) {
        // Do nothing.
    }

    private void refreshAdapter() {
        adapter.selected_usersList = multiselect_list;
        adapter.notifyDataSetChanged();
    }

    private void scanGallery() {
        Intent mediaScanIntent = new Intent(
                "android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        mediaScanIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getContext().sendBroadcast(mediaScanIntent);
    }

    private void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(adapter.data.get(position))){
                if(multiselect_list.size() == 1){
                    mActionMode.finish();
                    mActionMode = null;
                    isMultiSelect = false;
                    multiselect_list.clear();
                    refreshAdapter();
                    return;
                }
                multiselect_list.remove(adapter.data.get(position));
            }else{
                multiselect_list.add(adapter.data.get(position));
            }

            if (multiselect_list.size() > 0){
                mActionMode.setTitle("" + multiselect_list.size());
            }else{
                mActionMode.setTitle("");
            }

            refreshAdapter();

        }
    }

    private void deleteShots(){
        new ShotDeletor(new ArrayList<>(multiselect_list), getContext()).execute();
        scanGallery();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private void shareShots(){
        new ShotSharer(getContext(), multiselect_list).shareImage();
    }

    private void selectAll(){
        multiselect_list.clear();
        multiselect_list.addAll(adapter.data);
        refreshAdapter();
    }

    private ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.action_select_share:
                    shareShots();
                    return true;
                case R.id.action_select_delete:
                    deleteShots();
                    return true;
                case R.id.action_select_allselect:
                    selectAll();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list.clear();
            refreshAdapter();
        }
    };

}
