package io.zirui.nccamera.view;

import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import io.zirui.nccamera.R;
import io.zirui.nccamera.camera.Camera;
import io.zirui.nccamera.storage.LocalSPData;
import io.zirui.nccamera.storage.ShotSaver;
import io.zirui.nccamera.view.image_gallery.ImageGalleryFragment;

public class MainActivity extends AppCompatActivity{

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    ShotSaver shotSaver;

    @BindView(R.id.drawer) NavigationView navigationView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;

    // Location
    private LocationGooglePlayServicesProvider provider;
    private Location lastLocation;

    SmartLocation smartLocation;

    public String id;
    public String startDate;

    // Stats
    private long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (LocalSPData.loadRandomID(this) == null){
            LocalSPData.storeRandomID(this);
        }
        id = LocalSPData.loadRandomID(this).substring(0, 7);

        if (LocalSPData.loadStartDate(this) == null){
            LocalSPData.storeStartDate(this);
        }
        startDate = LocalSPData.loadStartDate(this);

        setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        // Check storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkAndRequestPermissions()){
                startLocation();
                replaceFragment();
            }
        } else {
            startLocation();
            replaceFragment();
        }

        // check usage stats permission.
        if(!showStats()){
            Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }

        lastLocation = SmartLocation.with(this).location().getLastLocation();

        shotSaver = ShotSaver.getInstance(this);

        setupDrawer();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Camera.takePhoto(MainActivity.this, shotSaver, lastLocation);
            }
        });

    }

    private boolean showStats(){
        long startTime = new GregorianCalendar(2014, 0, 1).getTimeInMillis();
        // long endTime = new GregorianCalendar(2016, 0, 1).getTimeInMillis();
        long endTime = System.currentTimeMillis();

        UsageStatsManager usageStatsManager = (UsageStatsManager)this.getSystemService(Activity.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);

        if(queryUsageStats.size() == 0){
            return false;
        }

        for (UsageStats us : queryUsageStats) {
            if(us.getPackageName().equals("io.zirui.nccamera")){
                duration = us.getTotalTimeInForeground();
            }
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showStats();
    }

    /**
     * Permissions.
     * */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkAndRequestPermissions(){
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, permission.PACKAGE_USAGE_STATS)
                != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(Manifest.permission.PACKAGE_USAGE_STATS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    /**
     * Replace the fragment in MainActivity.
     * */

    private void replaceFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content, ImageGalleryFragment.newInstance())
                .commit();
    }

    /**
     * Result handlers.
     * */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Camera.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            shotSaver.handleBigCameraPhoto();
        } else if (requestCode == LocalSPData.RQ_SM_CODE){
            LocalSPData.storeSurveyRecord(this);
            Toast.makeText(this, "Thanks for your time!", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:{
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startLocation();
                        replaceFragment();
                    }else{
                        checkAndRequestPermissions();
                    }
                }
            }
        }
    }

    /**
     * Navigation drawer settings.
     * */

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,          /* DrawerLayout object */
                R.string.open_drawer,         /* "open drawer" description */
                R.string.close_drawer         /* "close drawer" description */
        );

        drawerLayout.addDrawerListener(drawerToggle);

        View headerView = navigationView.getHeaderView(0);

        ((TextView) headerView.findViewById(R.id.nav_header_id)).setText(id);
        // ((TextView) headerView.findViewById(R.id.nav_header_startDate)).setText(startDate);
        ((TextView) headerView.findViewById(R.id.nav_header_startDate)).setText(Long.toString(duration / 50000) + "Minutes");
    }


    /**
     * Location data and permission request.
     * */

    private void startLocation() {
        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        smartLocation = new SmartLocation.Builder(this).logging(true).build();

        long mLocTrackingInterval = 0;
        float trackingDistance = 0;
        LocationAccuracy trackingAccuracy = LocationAccuracy.HIGH;

        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(trackingAccuracy)
                .setDistance(trackingDistance)
                .setInterval(mLocTrackingInterval);

        smartLocation
                .location(provider)
                .continuous()
                .config(builder.build())
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        lastLocation = location;
                }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocation();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        startLocation();
    }

    private void stopLocation() {
        SmartLocation.with(this).location().stop();
    }
}
