package alei.switchpro.brightness;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import alei.switchpro.Constants;
import alei.switchpro.R;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class LevelPreference extends DialogPreference implements OnCheckedChangeListener
{
    private Activity parent;
    private TextView currentLevel;
    private List<Integer> lastLevel;
    public static final String DEFAULT_LEVEL = "10,100,-1";
    private CheckBox level0;
    private CheckBox level10;
    private CheckBox level20;
    private CheckBox level30;
    private CheckBox level40;
    private CheckBox level50;
    private CheckBox level60;
    private CheckBox level70;
    private CheckBox level80;
    private CheckBox level90;
    private CheckBox level100;
    private CheckBox levelAuto;

    public interface OnColorChangedListener
    {
        void colorChanged(int color);
    }

    public LevelPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LevelPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    /**
     * 这个方法会在整个Preference实例化以后调用
     * 
     * @param parent
     */
    public void setActivity(Activity parent)
    {
        this.parent = parent;
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(parent);
        lastLevel = getLevelRealList(config.getString(Constants.PREFS_BRIGHT_LEVEL, DEFAULT_LEVEL));
        setSummary(getLevelViewStr(lastLevel));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        if (positiveResult)
        {
            if (lastLevel.size() < 2)
            {
                Toast.makeText(parent, R.string.level_select_warning, Toast.LENGTH_LONG).show();
                return;
            }

            setSummary(currentLevel.getText());
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(parent).edit();
            editor.putString(Constants.PREFS_BRIGHT_LEVEL, getLevelRealStr());
            editor.commit();
        }
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder)
    {
        super.onPrepareDialogBuilder(builder);
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(parent);
        lastLevel = getLevelRealList(config.getString(Constants.PREFS_BRIGHT_LEVEL, DEFAULT_LEVEL));

        builder.setTitle(R.string.bright_level);
        LayoutInflater inflater = parent.getLayoutInflater();
        View dlgView = inflater.inflate(R.layout.dlg_brightness_level, null, false);
        currentLevel = ((TextView) dlgView.findViewById(R.id.current_level));
        level0 = ((CheckBox) dlgView.findViewById(R.id.level_0));
        level10 = ((CheckBox) dlgView.findViewById(R.id.level_10));
        level20 = ((CheckBox) dlgView.findViewById(R.id.level_20));
        level30 = ((CheckBox) dlgView.findViewById(R.id.level_30));
        level40 = ((CheckBox) dlgView.findViewById(R.id.level_40));
        level50 = ((CheckBox) dlgView.findViewById(R.id.level_50));
        level60 = ((CheckBox) dlgView.findViewById(R.id.level_60));
        level70 = ((CheckBox) dlgView.findViewById(R.id.level_70));
        level80 = ((CheckBox) dlgView.findViewById(R.id.level_80));
        level90 = ((CheckBox) dlgView.findViewById(R.id.level_90));
        level100 = ((CheckBox) dlgView.findViewById(R.id.level_100));
        levelAuto = ((CheckBox) dlgView.findViewById(R.id.level_auto));
        level0.setOnCheckedChangeListener(this);
        level10.setOnCheckedChangeListener(this);
        level20.setOnCheckedChangeListener(this);
        level30.setOnCheckedChangeListener(this);
        level40.setOnCheckedChangeListener(this);
        level50.setOnCheckedChangeListener(this);
        level60.setOnCheckedChangeListener(this);
        level70.setOnCheckedChangeListener(this);
        level80.setOnCheckedChangeListener(this);
        level90.setOnCheckedChangeListener(this);
        level100.setOnCheckedChangeListener(this);
        levelAuto.setOnCheckedChangeListener(this);

        if (lastLevel.contains(0))
        {
            level0.setChecked(true);
        }
        if (lastLevel.contains(10))
        {
            level10.setChecked(true);
        }
        if (lastLevel.contains(20))
        {
            level20.setChecked(true);
        }
        if (lastLevel.contains(30))
        {
            level30.setChecked(true);
        }
        if (lastLevel.contains(40))
        {
            level40.setChecked(true);
        }
        if (lastLevel.contains(50))
        {
            level50.setChecked(true);
        }
        if (lastLevel.contains(60))
        {
            level60.setChecked(true);
        }
        if (lastLevel.contains(70))
        {
            level70.setChecked(true);
        }
        if (lastLevel.contains(80))
        {
            level80.setChecked(true);
        }
        if (lastLevel.contains(90))
        {
            level90.setChecked(true);
        }
        if (lastLevel.contains(100))
        {
            level100.setChecked(true);
        }
        if (lastLevel.contains(-1))
        {
            levelAuto.setChecked(true);
        }
        builder.setView(dlgView);
    }

    /*
     * 只在显示时调用
     * 
     * @see android.preference.Preference#onBindView(android.view.View)
     */
    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
    }

    public void onCheckedChanged(CompoundButton paramCompoundButton, boolean paramBoolean)
    {
        String txt = (String) paramCompoundButton.getText();

        if (txt.equals("0%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(0))
                {
                    lastLevel.add(0);
                }
            }
            else
            {
                lastLevel.remove(new Integer(0));
            }
        }
        if (txt.equals("10%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(10))
                {
                    lastLevel.add(10);
                }
            }
            else
            {
                lastLevel.remove(new Integer(10));
            }
        }
        else if (txt.equals("20%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(20))
                {
                    lastLevel.add(20);
                }
            }
            else
            {
                lastLevel.remove(new Integer(20));
            }
        }
        else if (txt.equals("30%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(30))
                {
                    lastLevel.add(30);
                }
            }
            else
            {
                lastLevel.remove(new Integer(30));
            }
        }
        else if (txt.equals("40%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(40))
                {
                    lastLevel.add(40);
                }
            }
            else
            {
                lastLevel.remove(new Integer(40));
            }
        }
        else if (txt.equals("50%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(50))
                {
                    lastLevel.add(50);
                }
            }
            else
            {
                lastLevel.remove(new Integer(50));
            }
        }
        else if (txt.equals("60%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(60))
                {
                    lastLevel.add(60);
                }
            }
            else
            {
                lastLevel.remove(new Integer(60));
            }
        }
        else if (txt.equals("70%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(70))
                {
                    lastLevel.add(70);
                }
            }
            else
            {
                lastLevel.remove(new Integer(70));
            }
        }
        else if (txt.equals("80%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(80))
                {
                    lastLevel.add(80);
                }
            }
            else
            {
                lastLevel.remove(new Integer(80));
            }
        }
        else if (txt.equals("90%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(90))
                {
                    lastLevel.add(90);
                }
            }
            else
            {
                lastLevel.remove(new Integer(90));
            }
        }
        else if (txt.equals("100%"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(100))
                {
                    lastLevel.add(100);
                }
            }
            else
            {
                lastLevel.remove(new Integer(100));
            }
        }
        else if (txt.equals("Auto"))
        {
            if (paramBoolean)
            {
                if (!lastLevel.contains(-1))
                {
                    lastLevel.add(-1);
                }
            }
            else
            {
                lastLevel.remove(new Integer(-1));
            }
        }

        currentLevel.setText(getLevelViewStr(lastLevel));
    }

    private String getLevelViewStr(List<Integer> levels)
    {
        String s = "";
        int pos = 0;

        for (Iterator<Integer> iterator = levels.iterator(); iterator.hasNext();)
        {
            int level = (iterator.next()).intValue();

            if (level != -1)
            {
                if (pos == 0)
                {
                    s = s + level;
                }
                else
                {
                    s = s + "->" + level;
                }
            }
            else
            {
                if (pos == 0)
                {
                    s = s + "Auto";
                }
                else
                {
                    s = s + "->" + "Auto";
                }
            }

            pos++;
        }

        return s;
    }

    private String getLevelRealStr()
    {
        String levelStr = "";
        int pos = 0;

        for (Iterator<Integer> iterator = lastLevel.iterator(); iterator.hasNext();)
        {
            int level = (iterator.next()).intValue();

            if (level != -1)
            {
                if (pos == 0)
                {
                    levelStr = levelStr + level;
                }
                else
                {
                    levelStr = levelStr + "," + level;
                }
            }
            else
            {
                if (pos == 0)
                {
                    levelStr = levelStr + "-1";
                }
                else
                {
                    levelStr = levelStr + "," + "-1";
                }
            }

            pos++;
        }

        return levelStr;
    }

    public static List<Integer> getLevelRealList(String realValue)
    {
        String[] level = realValue.split(",");
        List<Integer> list = new ArrayList<Integer>();

        for (int i = 0; i < level.length; i++)
        {
            list.add(Integer.parseInt(level[i]));
        }

        return list;
    }
}
