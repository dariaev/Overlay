package hackfest.overlay;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ChooseOverlayActivity extends ActionBarActivity {

    @InjectView(R.id.overlay) ImageView mOverlayPng;
    @InjectView(R.id.selected_photo) ImageView mSelectedPhoto;

    private final String TAG = ChooseOverlayActivity.class.getSimpleName();
    private SlidingUpPanelLayout SlidePanel = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_overlay);
        ButterKnife.inject(this);
        populateGallery();
        getSupportActionBar().hide();
        SlidePanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        SlidePanel.setOverlayed(true);
        SlidePanel.setAnchorPoint(400);
        SlidePanel.setShadowHeight(0);
        SlidePanel.setPanelHeight(0);
        // saveOverlayedImage();
        getTopNTrending(20);
    }

    public void PullUpSharedScreen(View view) {
        SlidePanel.setVisibility(View.VISIBLE);
        SlidePanel.setPanelHeight(600);
    }

    public void ShareViaEmail(View view) {
        BottomSheet share = new BottomSheet.Builder(this).title("Share").grid().sheet(R.menu.sharemenu).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap screenshot = Bitmap.createBitmap(65, 54, Bitmap.Config.RGB_565);

                String path = MediaStore.Images.Media.insertImage(getContentResolver(), screenshot, "title", null);
                Uri screenshotUri = Uri.parse(path);

                switch (which) {
                    case R.id.help:
                        //q.toast("Help me!");
                        break;
                    case R.id.Download:
                        break;
                    case R.id.Email:
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setType("plain/text");
                        sendIntent.setData(Uri.parse(""));
                        sendIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out my Swiper photo!");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this photo I took in Swiper!");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                        startActivity(sendIntent);
                        break;
                    case R.id.Twitter:
                        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                        tweetIntent.putExtra(Intent.EXTRA_TEXT, "Check out this photo I took in Swiper!");
                        tweetIntent.setType("text/plain");

                        PackageManager packManager = getPackageManager();
                        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

                        boolean resolved = false;
                        for (ResolveInfo resolveInfo : resolvedInfoList) {
                            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
                                tweetIntent.setClassName(
                                        resolveInfo.activityInfo.packageName,
                                        resolveInfo.activityInfo.name);
                                resolved = true;
                                break;
                            }
                        }
                        tweetIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);

                        startActivity(tweetIntent);

                        break;
                    case R.id.Facebook:
                        try {
                            Intent intent1 = new Intent();
                            intent1.setClassName("com.facebook.katana", "com.facebook.katana.activity.composer.ImplicitShareIntentHandler");
                            intent1.setAction("android.intent.action.SEND");
                            intent1.setType("text/plain");
                            intent1.putExtra(Intent.EXTRA_TEXT, "Check out this photo I took in Swiper!");
                            intent1.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                            startActivity(intent1);
                        } catch (Exception e) {
                            // If we failed (not native FB app installed), try share through SEND
                        }
                        break;
                }
            }
        }).build();

        share.show();
/*
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);

        sharingIntent.setType("image/png");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out my photo on Swiper!");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,"Hey!\nCheck out my photo on Swiper!");

        File image = new File(Environment.getExternalStorageDirectory()+"/DCIM/Camera/ed.png");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        startActivity(sharingIntent); */
    }
    private void saveOverlayedImage() {
        // processes two images, merges them and saves the result
        Bitmap selectedPhotoBitmap = imageViewToBitmap(mSelectedPhoto);
        Bitmap overlayBitmap = imageViewToBitmap(mOverlayPng);
        Bitmap bmOverlay = Bitmap.createBitmap(selectedPhotoBitmap.getWidth(),
                selectedPhotoBitmap.getHeight(), selectedPhotoBitmap.getConfig());
        Canvas canvas = new Canvas();
        canvas.setBitmap(bmOverlay);
        canvas.drawBitmap(selectedPhotoBitmap, new Matrix(), null);
        canvas.drawBitmap(overlayBitmap, new Matrix(), null);
        canvas.save();

        // saving the bitmap
        try {
            String fileName = Environment.getExternalStorageDirectory().toString();
            File overlayedImageFile = new File(fileName, "overlayedImage.png");
            overlayedImageFile.createNewFile();
            Log.i(TAG, "file created " + overlayedImageFile.toString());
            FileOutputStream out = new FileOutputStream(overlayedImageFile);
            bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (IOException e) {
            Log.e(TAG, "ERROR");
            e.printStackTrace();
        }
    }

    private Bitmap imageViewToBitmap(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        // Without it the view will have a dimension of 0,0 and the bitmap will be null
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        imageView.layout(0, 0, imageView.getMeasuredWidth(),
                imageView.getMeasuredHeight());

        imageView.buildDrawingCache();
        return imageView.getDrawingCache();
    }

    private void populateGallery() {
        LinearLayout imageGallery = (LinearLayout) findViewById(R.id.imageGallery);
        for (int i = 0; i < 10; i++) {
            imageGallery.addView(getImageView(R.drawable.angie_head_circle));
        }
    }

    private View getImageView(Integer image) {
        ImageView imageView = new ImageView(getApplicationContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200,200);
        lp.setMargins(0, 0, 50, 0);
        imageView.setLayoutParams(lp);
        imageView.setImageResource(image);
        return imageView;
    }


    //possible that this will return an empty list
    // Noah
    public ArrayList<Overlay> getTopNTrending(final int n) {
        final ArrayList<Overlay> trending = new ArrayList<Overlay>();
        ParseQuery query = new ParseQuery("Overlay");

        // by location temporarily looks for the top most popular iwthin a "30 mile radius"
        query.orderByDescending("UseCount");

        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, com.parse.ParseException e) {
                if (e == null) {

                    String myObject = list.toString();
                    Log.d("Angie", "Retrieved " + myObject + " Brands");

                } else {
                    Log.d("Angie", "Error: " + e.getMessage());
                }
            }

            @Override
            public void done(Object o, Throwable throwable) {
                List<ParseObject> list = (List<ParseObject>) o;
                for (int i = 0; i < n && i < list.size(); i++) {
                    ParseObject object = (ParseObject) list.get(i);
                    Overlay overlay = new Overlay(object);
                    trending.add(overlay);
                }

            }
        });
        return trending;
    }

    //possible that this will return an empty list
    // Noah
    public ArrayList<Overlay> getTopNByLocation(final int n, double longitude, double latitude) {
        final ArrayList<Overlay> topN = new ArrayList<Overlay>();
        ParseQuery query = new ParseQuery("Overlay");

        // by location temporarily looks for the top most popular iwthin a "30 mile radius"
        ParseGeoPoint searchQueryPoint = new ParseGeoPoint(latitude, longitude);
        query.whereWithinMiles("CurrentGeoPoint", searchQueryPoint,50);

        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, com.parse.ParseException e) {
                if (e == null) {

                    String myObject = list.toString();
                    Log.d("Angie", "Retrieved " + myObject + " Brands");

                } else {
                    Log.d("Angie", "Error: " + e.getMessage());
                }
            }

            @Override
            public void done(Object o, Throwable throwable) {
                List<ParseObject> list = (List<ParseObject>) o;
                for (int i = 0; i < n && i < list.size(); i++) {
                    ParseObject object = (ParseObject) list.get(i);
                    Overlay overlay = new Overlay(object);
                    topN.add(overlay);
                }

            }
        });
        return topN;

    }

    //possible that this will return an empty list
    public ArrayList<Overlay> getAllOverlays() {
        final ArrayList<Overlay> allOverlays = new ArrayList<Overlay>();
        ParseQuery query = new ParseQuery("Overlay");
        Log.d("Angie", "Retrieved  Brands");

        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, com.parse.ParseException e) {
                if (e == null) {

                    String myObject = list.toString();
                    Log.d("Angie", "Retrieved " + myObject + " Brands");


                } else {
                    Log.d("Angie", "Error: " + e.getMessage());
                }
            }

            @Override
            public void done(Object o, Throwable throwable) {
                Log.d("Angie", "Retrieved " + o.toString());
                List<ParseObject> list = (List<ParseObject>) o;
                for (int i=0; i<list.size(); i++) {
                    ParseObject object = (ParseObject) list.get(i);
                    Log.v("Angie", "ID" + object.getObjectId());
                    Overlay overlay = new Overlay(object);
                    allOverlays.add(overlay);
                }

            }
        });
        return allOverlays;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_overlay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
