package hackfest.overlay;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.cocosw.bottomsheet.BottomSheet;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ChooseOverlayActivity extends ActionBarActivity {

    @InjectView(R.id.overlay) ImageView mOverlayPng;
    @InjectView(R.id.selected_photo) ImageView mSelectedPhoto;
    @InjectView(R.id.imgButtonLeft) ImageView imgButtonLeft;
    @InjectView(R.id.imgButtonRight) ImageView imgButtonRight;
    @InjectView(R.id.textButtonMiddle) TextView textButtonMiddle;
    @InjectView(R.id.horizontalScrollView) HorizontalScrollView scrollView;
    private final String TAG = ChooseOverlayActivity.class.getSimpleName();
    final ArrayList<Overlay> topNlocation = new ArrayList<Overlay>();
    final ArrayList<Overlay> trending = new ArrayList<Overlay>();
    private SlidingUpPanelLayout SlidePanel = null;
    private GestureDetectorCompat mDetector;
    private boolean lastWasPrev;
    private ImageView previouslySelected;
    final ArrayList<Overlay> allOverlays = new ArrayList<Overlay>();
    ListIterator<Overlay> itr;
    LinearLayout imageGallery;
    private String left;
    private String mid;
    private String right;
    private int scrollIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_overlay);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        ButterKnife.inject(this);
        imageGallery = (LinearLayout) findViewById(R.id.imageGallery);

        byte[] input = ((OverlayApp) getApplication()).getImage();
        // input bitmap is off by 90 degrees
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(input, 0, input.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap,imageBitmap.getWidth(),
                imageBitmap.getHeight(),true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(),
                scaledBitmap.getHeight(), matrix, true);
        mSelectedPhoto.setImageBitmap(rotatedBitmap);
        getAllOverlays();
        lastWasPrev = false;
        getTopNByLocation(10,((OverlayApp)getApplication()).lastLong,((OverlayApp)getApplication()).lastLat);
        previouslySelected = null;
        getSupportActionBar().hide();
        left = "Search";
        mid = "Location";
        right = "Trending";
    }

    public void topNavClicked(View v) {
        if (v.equals(imgButtonLeft)){
            textButtonMiddle.setText(left);
            if (mid.equals("Location")) {
                imgButtonLeft.setImageResource(R.drawable.ic_location_on_black_24dp);
                mid = left;
                left = "Location";
            } else if (mid.equals("Search")) {
                imgButtonLeft.setImageResource(R.drawable.ic_search_black_24dp);
                mid = left;
                left = "Search";
            } else {
                imgButtonLeft.setImageResource(R.drawable.ic_trending_up_black_24dp);
                mid = left;
                left = "Trending";
            }
        } else if (v.equals(imgButtonRight)) {
            textButtonMiddle.setText(right);
            if (mid.equals("Location")) {
                imgButtonRight.setImageResource(R.drawable.ic_location_on_black_24dp);
                mid = right;
                right = "Location";
            } else if (mid.equals("Search")) {
                imgButtonRight.setImageResource(R.drawable.ic_search_black_24dp);
                mid = right;
                right = "Search";
            } else {
                imgButtonRight.setImageResource(R.drawable.ic_trending_up_black_24dp);
                mid = right;
                right = "Trending";
            }
        }
        if(mid.equals("Trending")) {
            getTopNTrending(10);
        } else if (mid.equals("Location")){
            getTopNByLocation(10,((OverlayApp)getApplication()).lastLong,((OverlayApp)getApplication()).lastLat);
        }
    }

    public void PullUpSharedScreen(View view) {
        SlidePanel.setVisibility(View.VISIBLE);
        SlidePanel.setPanelHeight(600);
}

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            int scrollIdx = -1;
            Log.d(DEBUG_TAG, "onFling: " + event1.toString()+event2.toString());
            if(velocityX > 0) {
                Overlay img;
                if (!itr.hasNext()) {
                    if(mid.equals("Location")) {
                        itr = topNlocation.listIterator();
                    } else if (mid.equals("Trending")) {
                        itr = trending.listIterator();
                    }
                }
                if (lastWasPrev) {
                    scrollIdx = itr.nextIndex();
                    img = itr.next();
                }
                Log.d("BLAH","BLAH");
                scrollIdx = itr.nextIndex();
                img = itr.next();
                Bitmap bmp = BitmapFactory.decodeByteArray(img.imageArray, 0, img.imageArray.length);
                Drawable d = new BitmapDrawable(getResources(), bmp);
                mOverlayPng.setImageDrawable(d);
                lastWasPrev = false;
            } else {
                Overlay img = null;
                if (!itr.hasPrevious()){
                    while(itr.hasNext()){
                        scrollIdx = itr.nextIndex();
                        img = itr.next();
                    }
                } else {
                    scrollIdx = itr.previousIndex();
                    img = itr.previous();
                }
                if (!lastWasPrev) {
                    scrollIdx = itr.previousIndex();
                    img = itr.previous();
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(img.imageArray, 0, img.imageArray.length);
                Drawable d = new BitmapDrawable(getResources(), bmp);
                mOverlayPng.setImageDrawable(d);
                Log.d("HERE","HERE");
                lastWasPrev = true;
            }
            if(scrollIdx != -1) scrollView.smoothScrollTo(scrollIdx*200+(scrollIdx-1)*50,0);
            return true;
        }
    }



    public void ShareViaEmail(View view) {
        BottomSheet share = new BottomSheet.Builder(this).title("Share").grid().sheet(R.menu.sharemenu).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap overlayedBitmap = getOverlayedBitmap(mSelectedPhoto, mOverlayPng);

                String path = MediaStore.Images.Media.insertImage(getContentResolver(), overlayedBitmap, "title", null);
                Uri screenshotUri = Uri.parse(path);

                switch (which) {
                    case R.id.help:
                        //q.toast("Help me!");
                        break;
                    case R.id.Download:
                        saveOverlayedImage(overlayedBitmap);
                        Toast.makeText(ChooseOverlayActivity.this, "Your image is saving", Toast.LENGTH_LONG).show();
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
    private void saveOverlayedImage(Bitmap bmOverlay) {
        // processes two images, merges them and saves the result

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

    private Bitmap getOverlayedBitmap(ImageView selectedPhoto, ImageView overlay) {
        Bitmap selectedPhotoBitmap =((BitmapDrawable)selectedPhoto.getDrawable()).getBitmap();
        Bitmap overlayBitmap = ((BitmapDrawable)overlay.getDrawable()).getBitmap();

        Bitmap bmOverlay = Bitmap.createBitmap(selectedPhotoBitmap.getWidth(),
                selectedPhotoBitmap.getHeight(), selectedPhotoBitmap.getConfig());
        Canvas canvas = new Canvas();
        canvas.setBitmap(bmOverlay);
        canvas.drawBitmap(selectedPhotoBitmap, new Matrix(), null);
        canvas.drawBitmap(overlayBitmap, new Matrix(), null);
        canvas.save();
        return bmOverlay;
    }

    private Bitmap imageViewToBitmap(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        // Without it the view will have a dimension of 0,0 and the bitmap will be null
        imageView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        imageView.layout(0, 0, imageView.getMeasuredWidth(),
                imageView.getMeasuredHeight());

        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        return bitmap;
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
                if (trending == null || trending.size() == 0) {
                    List<ParseObject> list = (List<ParseObject>) o;
                    for (int i = 0; i < n && i < list.size(); i++) {
                        ParseObject object = (ParseObject) list.get(i);
                        Overlay overlay = new Overlay(object);
                        trending.add(overlay);
                    }
                }
                imageGallery.removeAllViews();
                int idx = 0;
                for (Overlay img : trending) {
                    final int scrollIdx = idx;
                    Log.d("Noah","img read");
                    idx++;
                    ImageView iv = new ImageView(getApplicationContext());
                    Bitmap bmp = BitmapFactory.decodeByteArray(img.imageArray, 0, img.imageArray.length);
                    final Drawable d = new BitmapDrawable(getResources(), bmp);
                    iv.setImageDrawable(d);
                    iv.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            ImageView i = (ImageView) v;
                            mOverlayPng.setImageDrawable(d);
                            scrollView.smoothScrollTo(scrollIdx*200+(scrollIdx-1)*50,0);
                        }
                    });
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200,200);
                    lp.setMargins(0, 0, 50, 0);
                    iv.setLayoutParams(lp);
                    imageGallery.addView(iv);
                }
                lastWasPrev = false;
                itr = trending.listIterator();


            }
        });
        return trending;
    }

    //possible that this will return an empty list
    // Noah
    public ArrayList<Overlay> getTopNByLocation(final int n, double longitude, double latitude) {
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
                if (trending == null || trending.size() == 0) {
                    List<ParseObject> list = (List<ParseObject>) o;
                    for (int i = 0; i < n && i < list.size(); i++) {
                        ParseObject object = (ParseObject) list.get(i);
                        Overlay overlay = new Overlay(object);
                        topNlocation.add(overlay);
                    }
                }
                imageGallery.removeAllViews();
                for (Overlay img : topNlocation) {
                    Log.d("Noah","img read");
                    ImageView iv = new ImageView(getApplicationContext());
                    Bitmap bmp = BitmapFactory.decodeByteArray(img.imageArray, 0, img.imageArray.length);
                    final Drawable d = new BitmapDrawable(getResources(), bmp);
                    iv.setImageDrawable(d);
                    iv.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if(previouslySelected != null){
                                previouslySelected.setBackgroundResource(Color.TRANSPARENT);
                                previouslySelected = (ImageView) v;
                            }
                            ImageView i = (ImageView) v;
                            previouslySelected = i;
                            v.setBackgroundResource(R.color.background_material_dark);
                            mOverlayPng.setImageDrawable(d);
                        }
                    });
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200,200);
                    lp.setMargins(0, 0, 50, 0);
                    iv.setLayoutParams(lp);
                    imageGallery.addView(iv);
                }
                lastWasPrev = false;
                itr = topNlocation.listIterator();

            }
        });
        return topNlocation;

    }

    //possible that this will return an empty list
    public ArrayList<Overlay> getAllOverlays() {
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
                for (Overlay img : allOverlays) {
                    Log.d("Noah","img read");
                    ImageView iv = new ImageView(getApplicationContext());
                    Bitmap bmp = BitmapFactory.decodeByteArray(img.imageArray, 0, img.imageArray.length);
                    final Drawable d = new BitmapDrawable(getResources(), bmp);
                    iv.setImageDrawable(d);
                    iv.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if(previouslySelected != null){
                                previouslySelected.setBackgroundResource(Color.TRANSPARENT);
                                previouslySelected = (ImageView) v;
                            }
                            ImageView i = (ImageView) v;
                            previouslySelected = i;
                            v.setBackgroundResource(R.color.background_material_dark);
                            mOverlayPng.setImageDrawable(d);
                        }
                    });
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200,200);
                    lp.setMargins(0, 0, 50, 0);
                    iv.setLayoutParams(lp);
                    imageGallery.addView(iv);
                }
                lastWasPrev = false;
                itr = allOverlays.listIterator();
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
