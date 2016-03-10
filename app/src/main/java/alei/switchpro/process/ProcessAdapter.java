package alei.switchpro.process;

import java.util.ArrayList;

import alei.switchpro.R;
import android.content.ContentValues;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ProcessAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;
    private ArrayList<ProcessData> mDataList;
    private ProcessMainActivity mContext;

    public ProcessAdapter(ProcessMainActivity context, ArrayList<ProcessData> list)
    {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mDataList = list;
    }

    public int getCount()
    {
        return mDataList.size();
    }

    public Object getItem(int position)
    {
        return mDataList.get(position);
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.item_process, null);

            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.list_icon);
            holder.text_name = (TextView) convertView.findViewById(R.id.list_name);
            holder.text_size = (TextView) convertView.findViewById(R.id.list_size);
            holder.text_type = (TextView) convertView.findViewById(R.id.list_type);
            holder.check_box = (CheckBox) convertView.findViewById(R.id.check_box);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        ProcessData processData = mDataList.get(position);

        holder.icon.setImageDrawable(processData.icon);
        holder.text_name.setText(processData.name);
        holder.text_size.setText(processData.memory);
        holder.text_type.setText(processData.importanceText);
        holder.check_box.setChecked(mContext.dbOper.isIgnored(processData.name));

        final String name = processData.name;
        holder.check_box.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                CheckBox checkBox = (CheckBox) v;

                if (checkBox.isChecked())
                {
                    checkBox.setChecked(true);
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("name", name);
                    mContext.dbOper.insertIgnoredApp(initialValues);
                }
                else
                {
                    checkBox.setChecked(false);
                    mContext.dbOper.deleteIgnoredApp(name);
                }
            }
        });

        return convertView;
    }

    private static class ViewHolder
    {
        CheckBox check_box;
        ImageView icon;
        TextView text_name;
        TextView text_size;
        TextView text_type;
    }
}
