package io.zirui.nccamera.storage;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import io.zirui.nccamera.R;
import io.zirui.nccamera.model.Shot;

public class ShotSharer {

    private final Context context;
    private final ArrayList<Uri> uris;

    public ShotSharer(Context context, List<Shot> shots){
        this.context = context;
        this.uris = shotToUri(shots);
    }

    public ShotSharer(Context context, Shot shot){
        this.context = context;
        this.uris = new ArrayList<>();
        uris.add(shot.uri);
    }

    private ArrayList<Uri> shotToUri(List<Shot> shots){
        ArrayList<Uri> imageUris = new ArrayList<>();
        for (Shot shot : shots){
            imageUris.add(shot.uri);
        }
        return imageUris;
    }

    public void shareImage(){
        Intent shareIntent = new Intent();
        if (uris.size() == 1){
            Uri imageUri = uris.get(0);
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/jpeg");
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_shot)));
        }else {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            shareIntent.setType("image/*");
            context.startActivity(Intent.createChooser(shareIntent, "Share images to.."));
        }
    }
}
