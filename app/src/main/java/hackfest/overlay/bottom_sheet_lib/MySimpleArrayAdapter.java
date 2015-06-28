package hackfest.overlay.bottom_sheet_lib;

/**
 * Created by sunangel on 6/28/15.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import hackfest.overlay.R;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public MySimpleArrayAdapter(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.personalInfo).findViewById(R.id.icon);
        TextView name = (TextView) rowView.findViewById(R.id.personalInfo).findViewById(R.id.name);
        TextView pts = (TextView) rowView.findViewById(R.id.personalInfo).findViewById(R.id.points);
        ImageView bigpic= (ImageView) rowView.findViewById(R.id.bigphoto);
        // change the icon for Windows and iPhone
        String s = values[position];
        if (position==0) {
            imageView.setImageResource(R.drawable.angie_head_circle);
            bigpic.setImageResource(R.drawable.angie_head_circle);
            name.setText("Angie Sun");
            pts.setText("51 points");
        } else if (position==1) {
            imageView.setImageResource(R.drawable.angie_head_circle);
            bigpic.setImageResource(R.drawable.angie_head_circle);
            name.setText("Daria Evdokimova");
            pts.setText("62 points");
        }else if (position==2){
            imageView.setImageResource(R.drawable.angie_head_circle);
            bigpic.setImageResource(R.drawable.angie_head_circle);
            name.setText("Rohan Das");
            pts.setText("900 points");
        }else if (position==3){
            imageView.setImageResource(R.drawable.angie_head_circle);
            bigpic.setImageResource(R.drawable.angie_head_circle);
            name.setText("Noah Presler");
            pts.setText("100 points");
        }else if (position==4){
        imageView.setImageResource(R.drawable.angie_head_circle);
        bigpic.setImageResource(R.drawable.angie_head_circle);
        name.setText("Angie Sun");
        pts.setText("50 points");
    }

        return rowView;
    }
}
