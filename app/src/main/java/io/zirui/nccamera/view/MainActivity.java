package io.zirui.nccamera.view;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.zirui.nccamera.R;
import io.zirui.nccamera.camera.Camera;
import io.zirui.nccamera.storage.LocalSPData;
import io.zirui.nccamera.storage.ShotSaver;
import io.zirui.nccamera.view.image_gallery.ImageGalleryFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_STORAGE = 0;

    ShotSaver shotSaver;

    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;

    private ActionBarDrawerToggle drawerToggle;

    public String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (LocalSPData.loadRandomID(this) == null){
            LocalSPData.storeRandomID(this);
        }
        id = LocalSPData.loadRandomID(this);

        setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setHomeButtonEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        } else {
            replaceFragment();
        }

        shotSaver = ShotSaver.getInstance(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Camera.takePhoto(MainActivity.this, shotSaver);
            }
        });
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_READ_STORAGE);
    }

    private void replaceFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content, ImageGalleryFragment.newInstance())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    replaceFragment();
                } else {
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_LONG)
                            .show();
                    requestStoragePermission();
                }
            }
        }
    }

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

//    private void setupDrawer() {
//        drawerToggle = new ActionBarDrawerToggle(
//                this,                  /* host Activity */
//                drawerLayout,          /* DrawerLayout object */
//                R.string.open_drawer,         /* "open drawer" description */
//                R.string.close_drawer         /* "close drawer" description */
//        );
//
//        drawerLayout.addDrawerListener(drawerToggle);
//
//        //Set up click listener for drawer
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                if (item.isChecked()){
//                    drawerLayout.closeDrawers();
//                    return true;
//                }
//
//                View headerView = navigationView.getHeaderView(0);
//
//                ((TextView) headerView.findViewById(R.id.nav_header_text)).setText(
//                        Dribbble.getCurrentUser().name);
//
//                Fragment fragment = null;
//                switch (item.getItemId()){
//                    case R.id.drawer_item_home:
//                        fragment = ShotListFragment.newInstance();
//                        setTitle(R.string.title_home);
//                        break;
//                    case R.id.drawer_item_likes:
//                        fragment = ShotListFragment.newInstance();
//                        setTitle(R.string.title_likes);
//                        break;
//                    case R.id.drawer_item_buckets:
//                        fragment = BucketListFragment.newInstance();
//                        Toast.makeText(MainActivity.this, "buckets clicked", Toast.LENGTH_LONG).show();
//                        setTitle(R.string.title_buckets);
//                        break;
//                }
//
//                drawerLayout.closeDrawers();
//
//                if (fragment != null){
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.fragment_container, fragment)
//                            .commit();
//                    return true;
//                }
//
//                return false;
//            }
//        });
//    }

}
