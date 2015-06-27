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
<<<<<<< HEAD
        //enableLocalDatastore(this);
=======
        enableLocalDatastore(this);
>>>>>>> 71fa7ef207f296f863a0184205f7aec29904d2b5

        Parse.initialize(this, "2KJXTDF4lYeromW26zCntFAwqSfHlpHvsDIngyAy", "wVyECX1l2tyJUVhXTBwW73EJ12X1lpNdrzQrCsGh");
    }

}

