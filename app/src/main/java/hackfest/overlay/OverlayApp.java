package hackfest.overlay;

import android.app.Application;

import com.parse.Parse;

import static com.parse.Parse.*;

/**
 * Created by sunangel on 6/27/15.
 */
public class OverlayApp extends Application {
    
    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    public void setFlag(boolean new_value) {
        this.isNewPicture = new_value;
    }
    public boolean getFlag() {
        return this.isNewPicture;
    }
    private boolean isNewPicture;
    private byte[] image;
    public double lastLong=-1;
    public double lastLat=-1;

    @Override public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        enableLocalDatastore(this);

        Parse.initialize(this, "2KJXTDF4lYeromW26zCntFAwqSfHlpHvsDIngyAy", "wVyECX1l2tyJUVhXTBwW73EJ12X1lpNdrzQrCsGh");
    }

}

