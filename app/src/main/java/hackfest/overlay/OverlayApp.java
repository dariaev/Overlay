package hackfest.overlay;

import android.app.Application;

/**
 * Created by sunangel on 6/27/15.
 */
public class OverlayApp extends Application {
    @Override public void onCreate() {
        super.onCreate();

// Enable Local Datastore.
        //enableLocalDatastore(this);

        //Parse.initialize(this, "APPLICATION ID", "CLIENT KEY");
    }

}

