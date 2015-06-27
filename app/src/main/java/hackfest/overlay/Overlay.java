package hackfest.overlay;

import android.util.Log;

import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by sunangel on 6/27/15.
 */
public class Overlay {
    private long TimeUploaded;
    private long LatAtUpload;
    private long LongAtUpload;
    private String FilePath;
    private String ParseObjectId;
    private String Title;
    private ArrayList<String> tags;
    public byte[] imageArray;

    //test constructor DO NOT USE
    public Overlay(String title, String objectID, long longitude, long latitude, long time) {
        TimeUploaded=time;
        LatAtUpload=latitude;
        LongAtUpload=longitude;
        FilePath="";
        ParseObjectId=objectID;
        Title=title;
    }

    public Overlay(ParseObject obj) {
        ParseFile parseFile= obj.getParseFile("ImageFile");
        try {
            imageArray = parseFile.getData();
        }catch (Exception e) {
            Log.v("Angie", e.toString());
        }
    }
}
