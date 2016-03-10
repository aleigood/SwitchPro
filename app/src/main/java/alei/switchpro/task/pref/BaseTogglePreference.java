package alei.switchpro.task.pref;

import alei.switchpro.R;
import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class BaseTogglePreference extends ListPreference implements OnClickListener
{
    private boolean isChecked;

    public BaseTogglePreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public void initData()
    {
        setOnPreferenceChangeListener(new OnPreferenceChangeListener()
        {
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                String[] values = (String[]) getEntryValues();

                for (int i = 0; i < values.length; i++)
                {
                    if (values[i].equals(newValue.toString()))
                    {
                        setSummary(getEntries()[i].toString());
                        setValue(newValue.toString());
                        break;
                    }
                }

                return true;
            }
        });
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.pref_check);

        if (checkBox != null)
        {
            checkBox.setChecked(isChecked);
            checkBox.setOnClickListener(this);
        }
    }

    public boolean isChecked()
    {
        return isChecked;
    }

    public void setChecked(boolean b)
    {
        isChecked = b;
    }

    public void onClick(View paramView)
    {
        isChecked = ((CheckBox) paramView).isChecked();
    }
}
