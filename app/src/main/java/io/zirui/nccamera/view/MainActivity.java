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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
import io.zirui.nccamera.AnalyticsApplication;
import io.zirui.nccamera.R;
import io.zirui.nccamera.camera.Camera;
import io.zirui.nccamera.storage.LocalSPData;
import io.zirui.nccamera.storage.ShotSaver;
import io.zirui.nccamera.storage.activityRecorder;
import io.zirui.nccamera.view.image_gallery.ImageGalleryFragment;

public class MainActivity extends AppCompatActivity{

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    ShotSaver shotSaver;

    // Image Firebase Database
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference userStorageRef;

    // Firebase Database/References
    private FirebaseDatabase database;
    private DatabaseReference dataRef;
    private DatabaseReference dataUser;
    private DatabaseReference dataSession;

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
    public int sessionCount;

    public long initialStartTime;
    public long returnStartTime;

    Bundle bundle;

    // indicator for new database entries, so that information doesn't override
    public int[] activitySwap;
    public int ignoreThree;
    public activityRecorder actRec;

    // Stats
    private long duration;

    // Google Analytics
    private Tracker mTracker;


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
        sessionCount = LocalSPData.loadAndStoreSession(this);

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

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.enableAutoActivityTracking(true);
        mTracker.set("&uid", id);

        // check usage stats permission.
        //        if(!showStats()){
        //            Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
        //            startActivity(intent);
        //        }

        initialStartTime = System.currentTimeMillis();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        userStorageRef = storageRef.child(id);



        // Set up database and create the structure of the database
        database = FirebaseDatabase.getInstance();
        dataRef = database.getReference();
        dataUser = dataRef.child(id);
        if(dataUser.getKey() != id){
            dataUser.setValue(id);
        }
        String stringSessionCount = String.valueOf(sessionCount);
        dataSession = dataUser.child(stringSessionCount);
        dataSession.setValue(stringSessionCount);
        activitySwap = new int[3];
        // Three initial calls when opening the app to ignore
        ignoreThree = 3;
        actRec = new activityRecorder(false, false, false , false);
        bundle = new Bundle();

        lastLocation = SmartLocation.with(this).location().getLastLocation();

        shotSaver = ShotSaver.getInstance(this);

        setupDrawer();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actRec.setOnCamera(true);
                Camera.takePhoto(MainActivity.this, shotSaver, lastLocation);
            }
        });
    }

    /**
     * Google Analytics
     */

    private void sendScreenImageName() {
        String name = "MainActivity";

        // [START screen_view_hit]
        Log.i(TAG, "Setting screen name: " + name);
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]
    }

    /**
     * UsageStats
     */

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
        ((TextView) headerView.findViewById(R.id.nav_header_startDate)).setText(startDate);
        // ((TextView) headerView.findViewById(R.id.nav_header_duration)).setText(Long.toString(duration / 50000) + " Minutes");
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

    private void stopLocation() {
        SmartLocation.with(this).location().stop();
    }

    @Override
    protected void onPause() {

        stopLocation();
        super.onPause();
    }

    @Override
    protected void onResume() {
        showStats();
        super.onResume();
    }

//    @Override
//    protected void onUserLeaveHint()
//    {
//        Log.e("onUserLeaveHint", "Home button pressed");
//        actRec.setHomeOrSwitch(false);
//        super.onUserLeaveHint();
//    }
}