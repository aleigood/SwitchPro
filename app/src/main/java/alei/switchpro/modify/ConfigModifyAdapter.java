package alei.switchpro.modify;

import alei.switchpro.R;
import alei.switchpro.WidgetProviderUtil;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

public class ConfigModifyAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();
    private Context context;

    public ConfigModifyAdapter(Activity activity, SparseIntArray map) {
        super();
        context = activity;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < map.size(); i++) {
            Integer widgetId = map.keyAt(i);
            RemoteViews remoteView = WidgetProviderUtil.buildAndUpdateButtons(activity, widgetId,
                    PreferenceManager.getDefaultSharedPreferences(activity), null);

            if (remoteView != null) {
                ListItem item = new ListItem(widgetId, remoteView, activity);
                mItems.add(item);
            }
        }
    }

    // ����ÿһ��ѡ���view
    public View getView(int position, View convertView, ViewGroup parent) {
        final ListItem item = (ListItem) getItem(position);
        convertView = mInflater.inflate(R.layout.item_widget, parent, false);

        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.widget_item);
        View widgetView = item.widgetView.apply(context, layout);
        widgetView.findViewById(R.id.btn_0).setClickable(false);
        widgetView.findViewById(R.id.btn_0).setFocusable(false);
        widgetView.findViewById(R.id.btn_0).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_1).setClickable(false);
        widgetView.findViewById(R.id.btn_1).setFocusable(false);
        widgetView.findViewById(R.id.btn_1).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_2).setClickable(false);
        widgetView.findViewById(R.id.btn_2).setFocusable(false);
        widgetView.findViewById(R.id.btn_2).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_3).setClickable(false);
        widgetView.findViewById(R.id.btn_3).setFocusable(false);
        widgetView.findViewById(R.id.btn_3).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_4).setClickable(false);
        widgetView.findViewById(R.id.btn_4).setFocusable(false);
        widgetView.findViewById(R.id.btn_4).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_5).setClickable(false);
        widgetView.findViewById(R.id.btn_5).setFocusable(false);
        widgetView.findViewById(R.id.btn_5).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_6).setClickable(false);
        widgetView.findViewById(R.id.btn_6).setFocusable(false);
        widgetView.findViewById(R.id.btn_6).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_7).setClickable(false);
        widgetView.findViewById(R.id.btn_7).setFocusable(false);
        widgetView.findViewById(R.id.btn_7).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_8).setClickable(false);
        widgetView.findViewById(R.id.btn_8).setFocusable(false);
        widgetView.findViewById(R.id.btn_8).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_9).setClickable(false);
        widgetView.findViewById(R.id.btn_9).setFocusable(false);
        widgetView.findViewById(R.id.btn_9).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_10).setClickable(false);
        widgetView.findViewById(R.id.btn_10).setFocusable(false);
        widgetView.findViewById(R.id.btn_10).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_11).setClickable(false);
        widgetView.findViewById(R.id.btn_11).setFocusable(false);
        widgetView.findViewById(R.id.btn_11).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_12).setClickable(false);
        widgetView.findViewById(R.id.btn_12).setFocusable(false);
        widgetView.findViewById(R.id.btn_12).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_13).setClickable(false);
        widgetView.findViewById(R.id.btn_13).setFocusable(false);
        widgetView.findViewById(R.id.btn_13).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_14).setClickable(false);
        widgetView.findViewById(R.id.btn_14).setFocusable(false);
        widgetView.findViewById(R.id.btn_14).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_15).setClickable(false);
        widgetView.findViewById(R.id.btn_15).setFocusable(false);
        widgetView.findViewById(R.id.btn_15).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_16).setClickable(false);
        widgetView.findViewById(R.id.btn_16).setFocusable(false);
        widgetView.findViewById(R.id.btn_16).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_17).setClickable(false);
        widgetView.findViewById(R.id.btn_17).setFocusable(false);
        widgetView.findViewById(R.id.btn_17).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_18).setClickable(false);
        widgetView.findViewById(R.id.btn_18).setFocusable(false);
        widgetView.findViewById(R.id.btn_18).setBackgroundResource(0);

        widgetView.findViewById(R.id.btn_19).setClickable(false);
        widgetView.findViewById(R.id.btn_19).setFocusable(false);
        widgetView.findViewById(R.id.btn_19).setBackgroundResource(0);

        layout.addView(widgetView);
        return convertView;
    }

    // ������Ŀ�ĸ���
    public int getCount() {
        return mItems.size();
    }

    // ������Ŀ��λ�û�ȡ��Ŀ������
    public Object getItem(int position) {
        return mItems.get(position);
    }

    // ��ȡĳһλ�õ�Id
    public long getItemId(int position) {
        return position;
    }

    public List<ListItem> getItems() {
        return mItems;
    }

    /**
     * Specific item in our list.
     */
    public class ListItem {
        public int widgetId;
        public RemoteViews widgetView;

        public ListItem(int widgetId, RemoteViews widgetView, Context context) {
            this.widgetId = widgetId;
            this.widgetView = widgetView;
        }
    }
}
