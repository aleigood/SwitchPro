package alei.switchpro.color;

import alei.switchpro.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ColorAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();

    /**
     * IND_COLOR_PINK = 0; IND_COLOR_RED = 1; IND_COLOR_YELLOW = 2;
     * IND_COLOR_DEFAULT = 3; IND_COLOR_GREEN = 4; IND_COLOR_LIGHTBLUE = 5;
     * IND_COLOR_BLUE = 6; IND_COLOR_PURPLE = 7; IND_COLOR_ORANGE = 8;
     * IND_COLOR_GRAY = 9;
     *
     * @param newValue
     */
    public ColorAdapter(PreferenceActivity activity) {
        super();
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources res = activity.getResources();

        // 顺序不能错，根据
        mItems.add(new ListItem(res, R.string.color_pink, R.drawable.ind_pink_on_c));
        mItems.add(new ListItem(res, R.string.color_red, R.drawable.ind_red_on_c));
        mItems.add(new ListItem(res, R.string.color_orange, R.drawable.ind_orange_on_c));
        mItems.add(new ListItem(res, R.string.color_yellow, R.drawable.ind_yellow_on_c));
        mItems.add(new ListItem(res, R.string.color_default, R.drawable.ind_on_c));
        mItems.add(new ListItem(res, R.string.color_green, R.drawable.ind_green_on_c));
        mItems.add(new ListItem(res, R.string.color_lightblue, R.drawable.ind_lightblue_on_c));
        mItems.add(new ListItem(res, R.string.color_blue, R.drawable.ind_blue_on_c));
        mItems.add(new ListItem(res, R.string.color_purple, R.drawable.ind_purple_on_c));
        mItems.add(new ListItem(res, R.string.color_gray, R.drawable.ind_gray_on_c));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = (ListItem) getItem(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_image, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.pref_current_txt);
        textView.setTag(item);
        textView.setText(item.text);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.pref_current_img);
        imageView.setImageDrawable(item.image);

        return convertView;
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Specific item in our list.
     */
    public class ListItem {
        public final CharSequence text;
        public final Drawable image;

        public ListItem(Resources res, int textResourceId, int imageResourceId) {
            text = res.getString(textResourceId);

            if (imageResourceId != -1) {
                image = res.getDrawable(imageResourceId);
            } else {
                image = null;
            }
        }
    }

}
