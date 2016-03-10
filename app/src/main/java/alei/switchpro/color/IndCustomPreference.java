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

public class IndCustomPreference extends Preference {
    private WidgetConfigBaseActivity parent;
    private ImageView preview_img;
    private int lastColor;
    private Dialog dlg;

    public IndCustomPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public IndCustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IndCustomPreference(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        parent = (WidgetConfigBaseActivity) context;

        // ���һ����ɫ
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(parent);
        // ��ͼȥ��ȡ���Widget�Ѿ����ڵ���ɫ���ã�����Ҳ����ͷ������һ�ε�����
        lastColor = config.getInt(String.format(Constants.PREFS_IND_COLOR_FIELD_PATTERN, this.parent.getWidgetId()),
                config.getInt(Constants.PREFS_LAST_IND_COLOR, Constants.IND_COLOR_DEFAULT));
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
        builder.setTitle(R.string.indicator_color);
        String layoutName = parent.listLayout.getValue();
        AlertDialog dialog;

        if (layoutName.equals(this.parent.layoutCustom) || layoutName.equals(this.parent.layoutCustomShadow)
                || layoutName.equals(this.parent.layoutNoBack)) {
            OnColorChangedListener listener = new OnColorChangedListener() {
                public void colorChanged(int color) {
                    applyAction(color);
                }
            };

            // ��Ҫ������ ��ɫ��Ĳ���
            LinearLayout layout = new LinearLayout(getContext());
            layout.setPadding(0, 0, 0, 0);
            layout.setOrientation(LinearLayout.VERTICAL);

            // ���öԻ���ı���ͼƬ
            Bitmap bitmap = BitmapFactory.decodeResource(parent.getResources(), R.drawable.trans_bg);
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            drawable.setDither(true);
            layout.setBackgroundDrawable(drawable);

            // ���һ�����صı༭��Ϊ�˿��Դ򿪼��̣������޷���ʾ�����
            EditText hideEdit = new EditText(parent);
            hideEdit.setVisibility(View.GONE);
            layout.addView(hideEdit);
            layout.setId(android.R.id.widget_frame);

            Display display = parent.getWindowManager().getDefaultDisplay();
            // ��ֹ������ʱ����ʾ����
            int width = display.getWidth();

            if (display.getWidth() > display.getHeight()) {
                width = display.getHeight();
            }

            int x = ((width - width / 3) / 2);

            final ColorPickerView mCPView = new ColorPickerView(getContext(), listener,
                    lastColor == Constants.NOT_SHOW_FLAG ? Color.WHITE : lastColor, x, false);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.FILL_PARENT);
            params1.gravity = Gravity.CENTER;
            mCPView.setLayoutParams(params1);
            layout.setId(android.R.id.widget_frame);
            layout.addView(mCPView);

            ScrollView hsv = new ScrollView(getContext());
            hsv.setFadingEdgeLength(0);
            hsv.addView(layout);
            hsv.setMinimumWidth(width);
            builder.setView(hsv);

            builder.setPositiveButton(parent.getResources().getString(R.string.button_apply), new OnClickListener() {
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    applyAction(mCPView.getColor());
                }
            });
            builder.setNeutralButton(parent.getResources().getString(R.string.hide), new OnClickListener() {
                // ���Ҫ����ָʾ����������ɫΪConstants.NOT_SHOW_FLAG�����ֵ�ǰ�ȫ�ģ���Ϊ���Ǹ�͸����ɫ���ڽ��������޷���������ɫ��
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // �������һ�����õ���ɫ
                    PreferenceManager.getDefaultSharedPreferences(parent).edit()
                            .putInt(Constants.PREFS_LAST_IND_COLOR, Constants.NOT_SHOW_FLAG).commit();
                    applyAction(Constants.NOT_SHOW_FLAG);
                }
            });
            builder.setNegativeButton(parent.getResources().getString(R.string.button_cancel), new OnClickListener() {
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    dlg.dismiss();
                }
            });

            dialog = builder.create();
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

                public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2,
                                              int paramInt3) {
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
        } else {
            ColorAdapter mAdapter = new ColorAdapter(parent);
            builder.setAdapter(mAdapter, new OnClickListener() {
                public void onClick(DialogInterface arg0, int imgId) {
                    // �������һ�����õ�ͼƬ
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(parent).edit();
                    editor.putInt(Constants.PREFS_LAST_IND_COLOR, imgId);
                    editor.commit();
                    applyAction(imgId);
                }
            });

            // �����ʲô����?
            builder.setInverseBackgroundForced(true);
            dialog = builder.create();
        }

        return dialog;
    }

    private void applyAction(int currentColor) {
        lastColor = currentColor;
        updateView();
        dlg.dismiss();
        parent.updatePreView();
    }

    public void updateView() {
        if (parent.listLayout != null) {
            String layoutName = parent.listLayout.getValue();

            if (layoutName.equals(parent.layoutCustom) || layoutName.equals(parent.layoutCustomShadow)
                    || layoutName.equals(parent.layoutNoBack)) {
                if (lastColor >= 0 && lastColor <= 9) {
                    lastColor = Color.WHITE;
                }
            } else {
                if (lastColor < 0 || lastColor > 9) {
                    lastColor = Constants.IND_COLOR_DEFAULT;
                }
            }

            switch (lastColor) {
                case Constants.IND_COLOR_PINK:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_pink_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_pink);
                    break;
                case Constants.IND_COLOR_RED:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_red_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_red);
                    break;
                case Constants.IND_COLOR_ORANGE:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_orange_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_orange);
                    break;
                case Constants.IND_COLOR_YELLOW:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_yellow_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_yellow);
                    break;
                case Constants.IND_COLOR_DEFAULT:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_default);
                    break;
                case Constants.IND_COLOR_GREEN:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_green_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_green);
                    break;
                case Constants.IND_COLOR_LIGHTBLUE:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_lightblue_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_lightblue);
                    break;
                case Constants.IND_COLOR_BLUE:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_blue_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_blue);
                    break;
                case Constants.IND_COLOR_PURPLE:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_purple_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_purple);
                    break;
                case Constants.IND_COLOR_GRAY:
                    if (preview_img != null) {
                        preview_img.setImageResource(R.drawable.ind_gray_on_c);
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    }
                    setSummary(R.string.color_gray);
                    break;
                case Constants.NOT_SHOW_FLAG:
                    if (preview_img != null)
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    setSummary(parent.getResources().getString(R.string.hide));
                    break;
                // ��ط����ܵ����������ɫ��16����ֵ
                default:
                    if (preview_img != null)
                        preview_img.setBackgroundColor(lastColor);
                    setSummary("#" + (Integer.toHexString(lastColor) + "").toUpperCase());
                    break;
            }
        }
    }

    public int getLastColor() {
        return lastColor;
    }

    public void setLastColor(int indColor) {
        lastColor = indColor;
        updateView();
    }
}
