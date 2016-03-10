package alei.switchpro.color;

import alei.switchpro.Constants;
import alei.switchpro.R;
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

public class DividerCustomPreference extends Preference {
    private WidgetConfigBaseActivity parent;
    private ImageView preview_img;
    private int lastColor;
    private Dialog dlg;

    public DividerCustomPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DividerCustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DividerCustomPreference(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        this.parent = (WidgetConfigBaseActivity) context;

        // ���һ����ɫ
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(parent);
        lastColor = config.getInt(
                String.format(Constants.PREFS_DIVIDER_COLOR_FIELD_PATTERN, this.parent.getWidgetId()),
                config.getInt(Constants.PREFS_LAST_DIVIDER_COLOR, Constants.DEFAULT_DEVIDER_COLOR));
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
        builder.setTitle(R.string.divider_color);

        OnColorChangedListener listener = new OnColorChangedListener() {
            public void colorChanged(int color) {
                applyAction(color);
            }
        };

        // ��Ҫ������ ��ɫ��Ĳ���
        LinearLayout layout = new LinearLayout(getContext());
        layout.setPadding(0, 0, 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        Display display = parent.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        if (display.getWidth() > display.getHeight()) {
            width = display.getHeight();
        }

        int x = ((width - width / 3) / 2);

        final ColorPickerView mCPView = new ColorPickerView(getContext(), listener,
                lastColor == Constants.NOT_SHOW_FLAG ? Color.WHITE : lastColor, x, true);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        params1.gravity = Gravity.CENTER;
        mCPView.setLayoutParams(params1);
        mCPView.setFocusable(true);
        layout.addView(mCPView);

        // ���һ�����صı༭��Ϊ�˿��Դ򿪼��̣������޷���ʾ�����
        EditText hideEdit = new EditText(parent);
        hideEdit.setVisibility(View.GONE);
        layout.addView(hideEdit);
        layout.setId(android.R.id.widget_frame);

        // ���öԻ���ı���ͼƬ
        Bitmap bitmap = BitmapFactory.decodeResource(parent.getResources(), R.drawable.trans_bg);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        drawable.setDither(true);
        layout.setBackgroundDrawable(drawable);

        final ScrollView hsv = new ScrollView(getContext());
        hsv.setFadingEdgeLength(0);
        hsv.addView(layout);
        hsv.setMinimumWidth(width);
        builder.setView(hsv);

        builder.setPositiveButton(parent.getResources().getString(R.string.button_apply), new OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // �������úõ���ɫ
                applyAction(mCPView.getColor());
            }
        });
        builder.setNeutralButton(parent.getResources().getString(R.string.hide), new OnClickListener() {
            // ���Ҫ����ָʾ����������ɫΪConstants.NOT_SHOW_FLAG�����ֵ�ǰ�ȫ�ģ���Ϊ����͸����ɫ���ڽ��������޷���������ɫ��
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // �������һ�����õ���ɫ
                PreferenceManager.getDefaultSharedPreferences(parent).edit()
                        .putInt(Constants.PREFS_LAST_DIVIDER_COLOR, Constants.NOT_SHOW_FLAG).commit();
                lastColor = Constants.NOT_SHOW_FLAG;
                dlg.dismiss();
                parent.updatePreView();
            }
        });
        builder.setNegativeButton(parent.getResources().getString(R.string.button_cancel), new OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                dlg.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        View dlgView = dialog.getLayoutInflater().inflate(R.layout.view_color_dlg_title, null, false);
        final EditText editText = (EditText) dlgView.findViewById(R.id.color_code_editor);
        editText.setText((Integer.toHexString(lastColor) + "").toUpperCase());

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
                    mCPView.setColor(color);
                } catch (Exception e) {
                }
            }
        });

        dialog.setCustomTitle(dlgView);
        return dialog;
    }

    private void applyAction(int currentColor) {
        lastColor = currentColor;
        updateView();
        dlg.dismiss();
        parent.updatePreView();
    }

    public void updateView() {
        if (lastColor == Constants.NOT_SHOW_FLAG) {
            if (preview_img != null)
                preview_img.setBackgroundColor(Color.TRANSPARENT);
            setSummary(parent.getResources().getString(R.string.hide));
        } else {
            if (preview_img != null)
                preview_img.setBackgroundColor(lastColor);
            setSummary("#" + (Integer.toHexString(lastColor) + "").toUpperCase());
        }
    }

    public int getLastColor() {
        return lastColor;
    }

    public void setLastColor(int lastColor) {
        this.lastColor = lastColor;
        updateView();
    }

}
