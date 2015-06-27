package hackfest.overlay;

import android.app.Application;

import com.parse.Parse;

import static com.parse.Parse.*;

/**
 * Created by sunangel on 6/27/15.
 */
public class OverlayApp extends Application {
    @Override public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        enableLocalDatastore(this);

        Parse.initialize(this, "2KJXTDF4lYeromW26zCntFAwqSfHlpHvsDIngyAy", "wVyECX1l2tyJUVhXTBwW73EJ12X1lpNdrzQrCsGh");
    }

}

