package hackfest.overlay;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.ActionMenuItem;
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
import android.widget.SearchView;
import android.widget.Toast;
import android.view.View.OnClickListener;

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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.List;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hackfest.overlay.bottom_sheet_lib.BottomSheet;


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
    public static Activity thisAct = null;
    public static Handler mHandler;
    public static BottomSheet SearchViewPopup = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisAct=this;
        setContentView(R.layout.activity_choose_overlay);
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        ButterKnife.inject(this);
        imageGallery = (LinearLayout) findViewById(R.id.imageGallery);

        byte[] input = ((OverlayApp) getApplication()).getImage();
        // input bitmap is off by 90 degrees
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(input, 0, input.length);
        Matrix matrix = new Matrix();
        if (((OverlayApp)getApplication()).getFlag() == true) {
            matrix.postRotate(90);
        }
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

    public void backClicked(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void midClick(View v){
        TextView tv = (TextView) v;
        if (((TextView) v).getText().equals("Search")){
            ShowSearchSlider(null);
        }
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
        } else {
            ShowSearchSlider("Swiper");
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

    public static class SearchQueryTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        public Activity mActivity;
        public String mQuery;
        public SearchQueryTask(Activity activity, String query) {
            mActivity=activity;
            mQuery=query;
        }
    protected Integer doInBackground(String... queryString) {
        URL url = null;
        URLConnection connection=null;
        String tempQueryString="barack%20obama";
        Log.v("Angie", "query string is " + mQuery);
        try {
            url = new URL("https://ajax.googleapis.com/ajax/services/search/images?" +
                    "v=1.0&q="+mQuery+"&as_filetype=png&imgc=trans&rsz=4");
            connection = url.openConnection();
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject json = new JSONObject(builder.toString());
            Log.v("AngieJSON", json.toString());
            JSONObject responseData= (JSONObject) json.get("responseData");
            Log.v("AngieJSON2", responseData.toString());
            JSONArray resultsArray= (JSONArray) responseData.get("results");
            Log.v("AngieJSON3", resultsArray.toString());
            Message msgObj = mHandler.obtainMessage();
            Bundle b = new Bundle();
            for (int i=0 ;i<resultsArray.length(); i++)
            {
                String s = "URL"+i;
                JSONObject wtf= (JSONObject) resultsArray.get(i);
                b.putString(s, wtf.getString("tbUrl"));
                s = "fullResURL"+i;
                b.putString(s, wtf.getString("url"));
                Log.v("message", "sent another one  " + s + wtf.getString("url"));
            }
            b.putInt("Total", resultsArray.length());
            msgObj.setData(b);
            mHandler.sendMessage(msgObj);
            //Log.v("AngieJSON4", responseData.get("GsearchResultClass").toString());
        }
        catch(Exception e) {
            Log.v("AngieJSON", "Exception");
            Log.v("AngieJSon", e.toString() + e.getStackTrace().toString());
            e.printStackTrace();
        }

         //   url = new URL("https://ajax.googleapis.com/ajax/services/search/images?" +
        //            "v=1.0&q=barack%20obama&as_filetype=png&imgc=trans&start=4");


        return 3;
    }

    protected void onPostExecute(Integer integer) {
    }
}

    public static class DownloadImageTask extends AsyncTask<String, Void, Drawable> {

        private String mURL="";
        public DownloadImageTask(String fullUrl) {
            mURL=fullUrl;
        }

        protected Drawable doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(mURL).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.toString() + mURL);
            }
            Drawable d = new BitmapDrawable(thisAct.getResources(), mIcon11);

            return d;
        }

        protected void onPostExecute(Bitmap result) {
        }
    }

    public static void updateSearchSlider(String queryText) {
        new SearchQueryTask(thisAct, queryText).execute(queryText);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Log.v("Angie", "message" + message.getData().get("URL") + "   " + message.getData().getInt("Total"));
                // Associate searchable configuration with the SearchView

                for (int i=0; i<message.getData().getInt("Total"); i++) {
                    try {
                        Drawable d = new DownloadImageTask(message.getData().get("URL"+i).toString())
                                .execute(message.getData().get("URL"+i).toString()).get();
                        SearchViewPopup.getMenu().getItem(i).setIcon(d);
                    }
                    catch(Exception e) {

                    }
                }
                SearchViewPopup.invalidate();
                removeMessages(0);
                //message.
            }
        };
    }
    public static void ShowSearchSlider(String queryText) {
        new SearchQueryTask(thisAct, queryText).execute(queryText);
         mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(final Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                Log.v("Angie", "total " + message.getData().getInt("Total"));
                final String s1= message.getData().getString("fullResURL0");
                Log.v("Angie", "first link" + s1);
                final String s2= message.getData().getString("fullResURL1");
                Log.v("Angie", "first link" + s2);
                final String s3= message.getData().getString("fullResURL2");
                Log.v("Angie", "first link" + s3);
                final String s4= message.getData().getString("fullResURL3");
                Log.v("Angie", "first link" + s4);
                final String s5= message.getData().getString("fullResURL4");
                Log.v("Angie", "first link" + s5);
                final String s6= message.getData().getString("fullResURL5");
                Log.v("Angie", "first link" + s6);

                BottomSheet share = new BottomSheet.Builder(thisAct).title("Search").grid().sheet(R.menu.search_overlay).listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.Img1:
                                try {
                                    Drawable d = new DownloadImageTask(s1).execute(s1).get();
                                } catch (Exception e) {
                                }
                                break;
                            case R.id.Img2:
                                try {
                                    Drawable d = new DownloadImageTask(s2).execute(s2).get();
                                } catch (Exception e) {
                                }
                                break;
                            case R.id.Img3:
                                try {
                                    Drawable d = new DownloadImageTask(s3).execute(s3).get();
                                } catch (Exception e) {
                                }
                                break;
                            case R.id.Img4:
                                try {
                                    Drawable d = new DownloadImageTask(s4).execute(s4).get();
                                } catch (Exception e) {
                                }
                                break;
                            case R.id.Img5:
                                try {
                                    Drawable d = new DownloadImageTask(s5).execute(s5).get();
                                } catch (Exception e) {
                                }
                                break;
                            case R.id.Img6:
                                try {
                                    Drawable d = new DownloadImageTask(s6).execute(s6).get();
                                } catch (Exception e) {
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }).build();
                share.show();
                SearchViewPopup=share;
                // Associate searchable configuration with the SearchView

                for (int i=0; i<message.getData().getInt("Total"); i++) {
                    Log.v("Angie", "messagehandler" + message.getData().get("URL" + i));
                    try {
                        Drawable d = new DownloadImageTask(message.getData().get("URL"+i).toString())
                                .execute(message.getData().get("URL"+i).toString()).get();
                        share.getMenu().getItem(i).setIcon(d);
                    }
                    catch(Exception e) {

                    }
                }



                removeMessages(0);
                //message.
            }
        };
    }


    public void ShareViaEmail(View view) {
        BottomSheet share = new BottomSheet.Builder(this).title("Share").grid().sheet(R.menu.sharemenu).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap overlayedBitmap = getOverlayedBitmap(mSelectedPhoto, mOverlayPng);
                if (overlayedBitmap == null) return;

                String path = MediaStore.Images.Media.insertImage(getContentResolver(), overlayedBitmap, "title", null);
                Uri screenshotUri = Uri.parse(path);

                switch (which) {
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
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this photo I took with Swiper!");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                        startActivity(sendIntent);
                        break;
                    case R.id.Twitter:
                        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                        tweetIntent.putExtra(Intent.EXTRA_TEXT, "Check out this photo I took with Swiper!");
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
                        break;
                    case R.id.Instagram:
                        // Create the new Intent using the 'Send' action.
                        Intent share = new Intent(Intent.ACTION_SEND);

                        // Set the MIME type
                        share.setType("image/*");
                        share.setPackage("com.instagram.android");

                        // Add the URI and the caption to the Intent.
                        share.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                        share.putExtra(Intent.EXTRA_TEXT, "Check out this photo I took with Swiper!");

                        // Broadcast the Intent.
                        startActivity(share);
                        break;
                }
            }
        }).build();
        share.show();
        share.hideSearchBar(true);
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
        if (overlay.getDrawable() == null) {
            Toast.makeText(this, "Select an overlay", Toast.LENGTH_LONG).show();
            return null;
        } else {
            Bitmap overlayBitmap = ((BitmapDrawable) overlay.getDrawable()).getBitmap();
            Bitmap selectedPhotoBitmap =((BitmapDrawable)selectedPhoto.getDrawable()).getBitmap();
            float aspectRatio = selectedPhotoBitmap.getWidth() / (float) selectedPhotoBitmap.getHeight();
            int newWidth = (int) 1.4 * overlayBitmap.getWidth();
            int newHeight = (int) 1.4 * Math.round(overlayBitmap.getWidth() / aspectRatio);
            Bitmap scaledSelectedPhoto = Bitmap.createScaledBitmap(selectedPhotoBitmap, newWidth,
                    newHeight, true);
            Bitmap bmOverlay = Bitmap.createBitmap(scaledSelectedPhoto.getWidth(),
                    scaledSelectedPhoto.getHeight(), scaledSelectedPhoto.getConfig());
            Canvas canvas = new Canvas();
            canvas.setBitmap(bmOverlay);
            canvas.drawBitmap(scaledSelectedPhoto, new Matrix(), null);
            canvas.drawBitmap(overlayBitmap, new Matrix(), null);
            canvas.save();
            return bmOverlay;
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
