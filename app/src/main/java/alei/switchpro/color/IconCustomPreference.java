package alei.switchpro.color;

import alei.switchpro.Constants;
import alei.switchpro.R;
import alei.switchpro.Utils;
import alei.switchpro.WidgetConfigBaseActivity;
import alei.switchpro.color.ColorPickerView.OnColorChangedListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class IconCustomPreference extends Preference {
    private WidgetConfigBaseActivity parent;
    private ImageView preview_img;
    private int lastColor;
    private int lastTrans;
    private SeekBar seekBar;
    private Dialog dlg;

    public IconCustomPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public IconCustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconCustomPreference(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.parent = (WidgetConfigBaseActivity) context;
        // ��ͼȥ��ȡ���Widget�Ѿ����ڵ���ɫ���ã�����Ҳ����ͷ������һ�ε����ã��޸�Widgetʱʹ�ã�
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(parent);
        lastColor = config.getInt(String.format(Constants.PREFS_ICON_COLOR_FIELD_PATTERN, this.parent.getWidgetId()),
                config.getInt(Constants.PREFS_LAST_ICON_COLOR, Color.WHITE));

        // Ĭ�ϵ�͸����Ϊ120
        lastTrans = config.getInt(String.format(Constants.PREFS_ICON_TRANS_FIELD_PATTERN, this.parent.getWidgetId()),
                config.getInt(Constants.PREFS_LAST_ICON_TRANS, 120));

        updateView();
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        preview_img = (ImageView) view.findViewById(R.id.pref_current_img);
        updateView();
    }

    @Override
    protected void onClick() {
        super.onClick();
        dlg = createDialog();
        dlg.show();
    }

    private Dialog createDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(R.string.icon_color);

        // ����м�Բ���Ǵ���
        OnColorChangedListener listener = new OnColorChangedListener() {
            public void colorChanged(int color) {
                applyAction(color);
            }
        };

        // ��Ҫ������ ��ɫ��Ĳ���
        LinearLayout colorLayout = new LinearLayout(getContext());
        colorLayout.setPadding(0, 0, 0, 10);
        colorLayout.setOrientation(LinearLayout.VERTICAL);

        // ���öԻ���ı���ͼƬ
        Bitmap bitmap = BitmapFactory.decodeResource(parent.getResources(), R.drawable.trans_bg);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        drawable.setDither(true);
        colorLayout.setBackgroundDrawable(drawable);

        // �ޱ���ʱ��ѡ���͸��ѡ�����԰뾶ҪС
        Display display = parent.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        if (display.getWidth() > display.getHeight()) {
            width = display.getHeight();
        }

        // �����ɫ���
        int x = ((width - width / 3) / 2);
        final ColorPickerView mCPView = new ColorPickerView(getContext(), listener,
                lastColor == Constants.NOT_SHOW_FLAG ? Color.WHITE : lastColor, x, false);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        params1.gravity = Gravity.CENTER;
        mCPView.setLayoutParams(params1);
        colorLayout.addView(mCPView);

        // ���һ�����صı༭��Ϊ�˿��Դ򿪼��̣������޷���ʾ�����
        EditText hideEdit = new EditText(parent);
        hideEdit.setVisibility(View.GONE);
        colorLayout.addView(hideEdit);
        colorLayout.setId(android.R.id.widget_frame);

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout layoutSeekbarBox = new LinearLayout(getContext());
        layoutSeekbarBox.setLayoutParams(params2);
        layoutSeekbarBox.setOrientation(LinearLayout.VERTICAL);

        final TextView txtView = new TextView(parent);
        txtView.setPadding(18, 5, 18, 0);
        txtView.setTextColor(Color.BLACK);
        txtView.setText(parent.getString(R.string.the_transparency_when_disabled) + ":");
        layoutSeekbarBox.addView(txtView);

        seekBar = new SeekBar(getContext());
        seekBar.setPadding(18, 5, 18, 5);
        // ���ó�ʼֵ
        seekBar.setMax(255);
        seekBar.setProgress(lastTrans);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtView.setText(parent.getString(R.string.the_transparency_when_disabled) + ":");
            }
        });
        layoutSeekbarBox.addView(seekBar);
        colorLayout.setId(android.R.id.widget_frame);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setPadding(0, 0, 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(colorLayout);
        layout.addView(layoutSeekbarBox);

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.setFadingEdgeLength(0);
        scrollView.addView(layout);
        scrollView.setMinimumWidth(width);
        scrollView.setBackgroundColor(0XFFF5F5F5);
        builder.setView(scrollView);

        // ��ʼ����ť�¼�
        builder.setPositiveButton(parent.getResources().getString(R.string.button_apply), new OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                applyAction(mCPView.getColor());
            }
        });
        builder.setNeutralButton(parent.getResources().getString(R.string.no_color), new OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                applyAction(Constants.NOT_SHOW_FLAG);
                parent.updatePreView();
            }
        });
        builder.setNegativeButton(parent.getResources().getString(R.string.button_cancel), new OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                dlg.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        View dlgView = dialog.getLayoutInflater().inflate(R.layout.view_color_dlg_title, null, false);
        final EditText editText = (EditText) dlgView.findViewById(R.id.color_code_editor);
        editText.setText((Integer.toHexString(lastColor == Constants.NOT_SHOW_FLAG ? Color.WHITE : lastColor) + "")
                .toUpperCase());

        mCPView.setOnColorChangingListener(new ColorPickerView.onColorChangingListener() {
            public void onChange(int color) {
                editText.setText((Integer.toHexString(color) + "").toUpperCase());
            }
        });

        // �������������ɫʱ��̬�ı�ѡ������ɫ
        editText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
                setColor();
            }

            public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
                setColor();
            }

            public void afterTextChanged(Editable paramEditable) {
                setColor();
            }

            private void setColor() {
                try {
                    int color = Color.parseColor("#" + editText.getText().toString());
                    mCPView.setColor(Utils.setAlpha(color, false));
                } catch (Exception e) {
                }
            }
        });

        dialog.setCustomTitle(dlgView);
        return dialog;
    }

    private void applyAction(int currentColor) {
        lastColor = currentColor;
        lastTrans = seekBar.getProgress();
        updateView();
        dlg.dismiss();
        parent.updatePreView();
    }

    public void updateView() {
        if (lastColor == Constants.NOT_SHOW_FLAG) {
            if (preview_img != null)
                preview_img.setBackgroundColor(Color.TRANSPARENT);
            setSummary(parent.getResources().getString(R.string.no_color));
        } else {
            if (preview_img != null)
                preview_img.setBackgroundColor(lastColor);
            setSummary("#" + (Integer.toHexString(lastColor)).toUpperCase());
        }
    }

    public int getLastColor() {
        return lastColor;
    }

    public void setLastColor(int lastColor) {
        this.lastColor = lastColor;
        updateView();
    }

    public int getLastTrans() {
        return lastTrans;
    }

    public void setLastTrans(int lastTrans) {
        this.lastTrans = lastTrans;
        updateView();
    }
}
