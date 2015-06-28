package hackfest.overlay;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

/**
 * Created by rohandas on 6/27/15.
 */
//public class SavePhotoTask extends AsyncTask<Boolean, Void, Void> {
//    byte[] mData;
//    Context mContext;
//    byte[] mSrc;
//
//    SavePhotoTask(Context context, byte[] dest, byte[] src) {
//        mData = dest;
//        mContext = context;
//        mSrc = src;
//    }
//
//    @Override
//    protected  doInBackground(Boolean... params) {
//        mSrc = mData;
//        return null;
//    }
//
//
//    @Override
//    public void onPictureTaken(byte[] data, Camera mCamera) {
//        try {
//            //I guess you know how to save a file
//            PhotoUtils.saveImage(data);
//        } catch (final Exception e) {
//            //capture flying shit
//        }
//    }
//}