package alei.switchpro.task.pref;

import alei.switchpro.R;
import alei.switchpro.task.TaskModifyActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.SeekBar;

public class VolumePreference extends DialogPreference implements OnClickListener {
    private boolean isChecked;
    private TaskModifyActivity parent;
    private int percent;
    private SeekBar seekBar;

    public VolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initData(TaskModifyActivity activity) {
        parent = activity;
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setTitle("Volume");
        LayoutInflater inflater = parent.getLayoutInflater();
        View dlgView = inflater.inflate(R.layout.view_task_seekbar, null, false);
        seekBar = (SeekBar) dlgView.findViewById(R.id.seek_bar);
        // 设置初始值
        seekBar.setProgress(percent);
        builder.setView(dlgView);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            percent = seekBar.getProgress();
            setSummary(percent + " %");
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.pref_check);

        if (checkBox != null) {
            checkBox.setChecked(isChecked);
            checkBox.setOnClickListener(this);
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean b) {
        isChecked = b;
    }

    public void onClick(View paramView) {
        isChecked = ((CheckBox) paramView).isChecked();
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

}
