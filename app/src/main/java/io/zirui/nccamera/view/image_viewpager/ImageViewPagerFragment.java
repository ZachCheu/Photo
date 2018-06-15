package io.zirui.nccamera.view.image_viewpager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.zirui.nccamera.R;
import io.zirui.nccamera.model.Shot;
import io.zirui.nccamera.storage.ActivityRecorder;
import io.zirui.nccamera.utils.ModelUtils;


public class ImageViewPagerFragment extends Fragment{

    public static final String EXTRA_INITIAL_POS = "initial_pos";
    public static final String EXTRA_IMAGES = "images";

    public ImageViewPagerAdapter adapter;

    @BindView(R.id.shot_view_pager) ViewPager viewPager;

    public static ImageViewPagerFragment newInstance(Bundle bundle){
        ImageViewPagerFragment imageViewPagerFragment = new ImageViewPagerFragment();
        imageViewPagerFragment.setArguments(bundle);
        return imageViewPagerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);
        ButterKnife.bind(this, view);
        ActivityRecorder.onImage = true;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<Shot> data = ModelUtils.toObject(getArguments().getString(EXTRA_IMAGES), new TypeToken<List<Shot>>(){});
        int currentPos = getArguments().getInt(EXTRA_INITIAL_POS);
        adapter = new ImageViewPagerAdapter(getChildFragmentManager(), data);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPos);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    protected Shot getCurrentItem(){
        return adapter.shots.get(viewPager.getCurrentItem());
    }

}
