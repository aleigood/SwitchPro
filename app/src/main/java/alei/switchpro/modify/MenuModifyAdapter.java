package alei.switchpro.modify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import alei.switchpro.R;
import android.app.Activity;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuModifyAdapter extends BaseAdapter
{
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();

    /**
     * Specific item in our list.
     */
    public class ListItem
    {
        public int size;
        public String title;
        public boolean isSelected;

        public ListItem(int size, String title)
        {
            this.size = size;
            this.title = title;
        }
    }

    public MenuModifyAdapter(Activity activity, List<AppWidgetProviderInfo> itemList)
    {
        super();
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItems.add(new ListItem(1, activity.getResources().getString(R.string.app_nameX1)));
        mItems.add(new ListItem(2, activity.getResources().getString(R.string.app_nameX2)));
        mItems.add(new ListItem(3, activity.getResources().getString(R.string.app_nameX3)));
        mItems.add(new ListItem(4, activity.getResources().getString(R.string.app_nameX4)));
        mItems.add(new ListItem(5, activity.getResources().getString(R.string.app_nameX5)));

        for (Iterator<AppWidgetProviderInfo> iterator = itemList.iterator(); iterator.hasNext();)
        {
            AppWidgetProviderInfo menuInfo = iterator.next();

            if (menuInfo.label.equals(activity.getResources().getString(R.string.app_nameX1)))
            {
                mItems.get(0).isSelected = true;
            }
            else if (menuInfo.label.equals(activity.getResources().getString(R.string.app_nameX2)))
            {
                mItems.get(1).isSelected = true;
            }
            else if (menuInfo.label.equals(activity.getResources().getString(R.string.app_nameX3)))
            {
                mItems.get(2).isSelected = true;
            }
            else if (menuInfo.label.equals(activity.getResources().getString(R.string.app_nameX4)))
            {
                mItems.get(3).isSelected = true;
            }
            else if (menuInfo.label.equals(activity.getResources().getString(R.string.app_nameX5)))
            {
                mItems.get(4).isSelected = true;
            }
            else
            {
                continue;
            }
        }
    }

    // 返回每一个选项的view
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final ListItem item = (ListItem) getItem(position);
        convertView = mInflater.inflate(R.layout.item_image_checkbox, parent, false);
        ImageView imgView = (ImageView) convertView.findViewById(R.id.pref_img);
        imgView.setImageResource(R.drawable.icon);
        TextView textView = (TextView) convertView.findViewById(R.id.pref_txt);
        textView.setText(item.title);
        CheckBox boxView = (CheckBox) convertView.findViewById(R.id.pref_checkbox);
        boxView.setChecked(item.isSelected);

        boxView.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                item.isSelected = arg1;
            }
        });

        return convertView;
    }

    // 返回条目的个数
    public int getCount()
    {
        return mItems.size();
    }

    // 根据条目的位置获取条目的内容
    public Object getItem(int position)
    {
        return mItems.get(position);
    }

    // 获取某一位置的Id
    public long getItemId(int position)
    {
        return position;
    }

    public List<ListItem> getItems()
    {
        return mItems;
    }
}
