package io.zirui.nccamera.storage;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.zirui.nccamera.R;

public class ShotSaver {

    private static ShotSaver sInstance;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private String mCurrentPhotoPath;
    private Context context;
    private AlbumStorageDirFactory mAlbumStorageDirFactory;
    private String imageFileName;
    private String id;


    private ShotSaver(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        this.context = context;
        id = LocalSPData.loadRandomID(context).substring(0, 7);

    }

    public static synchronized ShotSaver getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ShotSaver(context);
            // sInstance = new ShotSaver(context.getApplicationContext());
        }
        return sInstance;
    }

    private String getAlbumName() {
        return context.getString(R.string.album_name);
    }

    public File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(context.getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    public File createImageFile(Location mLastLocation) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String locationStamp = mLastLocation != null ? mLastLocation.getLongitude() + "_" + mLastLocation.getLatitude(): "null";
        locationStamp = "[" + locationStamp + "]_";
        imageFileName = JPEG_FILE_PREFIX + timeStamp + "_" + locationStamp + "_" +id;
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        mCurrentPhotoPath = imageF.getAbsolutePath();
        return imageF;
    }

    public void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        String fileName = imageFileName+".png";
        String path = id +"/"+ fileName;
        // path to all images
        StorageReference all = FirebaseStorage.getInstance().getReference("all/"+fileName);

        // path to user
        StorageReference ref = FirebaseStorage.getInstance().getReference(path);
        StorageMetadata imgMetadata = new StorageMetadata.Builder()
                .setCustomMetadata("text", id + "_"+ imageFileName)
                .build();
        UploadTask allTask = all.putFile(contentUri,imgMetadata);
        UploadTask userTask = ref.putFile(contentUri, imgMetadata);
        context.sendBroadcast(mediaScanIntent);
    }

    public void handleBigCameraPhoto() {
        if (mCurrentPhotoPath != null) {
            galleryAddPic();
            mCurrentPhotoPath = null;
        }
    }
}
