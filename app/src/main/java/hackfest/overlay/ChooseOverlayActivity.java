package hackfest.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ChooseOverlayActivity extends ActionBarActivity {

    @InjectView(R.id.overlay)
    ImageView mOverlayPng;
    @InjectView(R.id.selected_photo) ImageView mSelectedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_overlay);
        ButterKnife.inject(this);

        Bitmap selectedPhotoBitmap = ((BitmapDrawable) mSelectedPhoto.getDrawable()).getBitmap();
        Bitmap overlayBitmap = ((BitmapDrawable) mSelectedPhoto.getDrawable()).getBitmap();

        Bitmap bmOverlay = Bitmap.createBitmap(selectedPhotoBitmap.getWidth(),
                selectedPhotoBitmap.getHeight(), selectedPhotoBitmap.getConfig());
        Canvas canvas = new Canvas();
        canvas.setBitmap(bmOverlay);
        canvas.drawBitmap(selectedPhotoBitmap, new Matrix(), null);
        canvas.drawBitmap(overlayBitmap, new Matrix(), null);
        canvas.save();
        // TODO figure out how to save it
        // this image view doesn't do anything right now
        ImageView testImage = new ImageView(this);
        testImage.setImageBitmap(bmOverlay);
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
