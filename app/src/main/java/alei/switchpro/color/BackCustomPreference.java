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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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

public class BackCustomPreference extends Preference
{
    private WidgetConfigBaseActivity parent;
    private ImageView preview_img;
    private int lastColor;
    private Dialog dlg;

    public BackCustomPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    public BackCustomPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public BackCustomPreference(Context context)
    {
        super(context);
        init(context);
    }

    public void init(Context context)
    {
        parent = (WidgetConfigBaseActivity) context;
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(parent);
        String backColorKey = String.format(Constants.PREFS_BACK_COLOR_FIELD_PATTERN, parent.getWidgetId());
        lastColor = config.getInt(backColorKey,
                config.getInt(Constants.PREFS_LAST_BACK_COLOR, Constants.DEFAULT_BACKGROUND_COLOR));
        updateView();
    }

    @Override
    public boolean isPersistent()
    {
        return false;
    }

    @Override
    protected void onBindView(View view)
    {
        super.onBindView(view);
        preview_img = (ImageView) view.findViewById(R.id.pref_current_img);
        updateView();
    }

    @Override
    protected void onClick()
    {
        String layoutName = parent.listLayout.getValue();

        if (layoutName.equals(parent.layoutDefault) || layoutName.equals(parent.layoutWhite))
        {
            final SeekBar seekbar = new SeekBar(parent);
            int pandding = Utils.dip2px(parent, 15);
            seekbar.setPadding(pandding, pandding, pandding, pandding);
            seekbar.setMax(255);
            seekbar.setProgress(lastColor);
            dlg = new AlertDialog.Builder(parent).setTitle(R.string.back_trans).setView(seekbar)
                    .setPositiveButton(android.R.string.ok, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            applyColorAction(seekbar.getProgress());
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
        }
        else
        {
            String[] items = new String[] { parent.getString(R.string.custom_color),
                    parent.getString(R.string.custom_image) };
            new AlertDialog.Builder(parent).setTitle(R.string.back_color).setItems(items, new OnClickListener()
            {
                public void onClick(DialogInterface arg0, int arg1)
                {
                    if (arg1 == 0)
                    {
                        dlg = createDialog();
                        dlg.show();
                    }
                    else
                    {
                        Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        Intent localIntent = new Intent("android.intent.action.PICK", localUri);
                        parent.startActivityForResult(localIntent, 1);
                    }
                }
            }).show();
        }
    }

    private Dialog createDialog()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(R.string.back_color);

        OnColorChangedListener listener = new OnColorChangedListener()
        {
            public void colorChanged(int color)
            {
                applyColorAction(color);
            }
        };

        // 主要是设置 调色板的布局
        LinearLayout layout = new LinearLayout(getContext());
        layout.setPadding(0, 0, 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        Display display = parent.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        if (display.getWidth() > display.getHeight())
        {
            width = display.getHeight();
        }

        int x = ((width - width / 3) / 2);

        final ColorPickerView mCPView = new ColorPickerView(getContext(), listener, lastColor, x, true);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        params1.gravity = Gravity.CENTER;
        mCPView.setLayoutParams(params1);
        mCPView.setFocusable(true);
        layout.addView(mCPView);

        // 添加一个隐藏的编辑框，为了可以打开键盘，否则无法显示软键盘
        EditText hideEdit = new EditText(parent);
        hideEdit.setVisibility(View.GONE);
        layout.addView(hideEdit);
        layout.setId(android.R.id.widget_frame);

        // 设置对话框的背景图片
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

        builder.setPositiveButton(parent.getResources().getString(R.string.button_apply), new OnClickListener()
        {
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                // 保存设置好的颜色
                applyColorAction(mCPView.getColor());
            }
        });
        builder.setNegativeButton(parent.getResources().getString(R.string.button_cancel), new OnClickListener()
        {
            public void onClick(DialogInterface paramDialogInterface, int paramInt)
            {
                dlg.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        View dlgView = dialog.getLayoutInflater().inflate(R.layout.view_color_dlg_title, null, false);
        final EditText editText = (EditText) dlgView.findViewById(R.id.color_code_editor);
        editText.setText((Integer.toHexString(lastColor) + "").toUpperCase());

        mCPView.setOnColorChangingListener(new ColorPickerView.onColorChangingListener()
        {
            public void onChange(int color)
            {
                editText.setText((Integer.toHexString(color) + "").toUpperCase());
            }
        });

        // 在输入框输入颜色时动态改变选择器颜色
        editText.addTextChangedListener(new TextWatcher()
        {
            public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3)
            {
                setColor();
            }

            public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3)
            {
                setColor();
            }

            public void afterTextChanged(Editable paramEditable)
            {
                setColor();
            }

            private void setColor()
            {
                try
                {
                    int color = Color.parseColor("#" + editText.getText().toString());
                    mCPView.setColor(color);
                }
                catch (Exception e)
                {
                }
            }
        });

        dialog.setCustomTitle(dlgView);
        return dialog;
    }

    private void applyColorAction(int currentColor)
    {
        // 设置颜色时把背景图值空，防止优先显示背景图
        parent.backBitmap = null;
        lastColor = currentColor;
        updateView();
        parent.updatePreView();
        dlg.dismiss();
    }

    public void updateView()
    {
        if (parent.listLayout != null)
        {
            String layoutName = parent.listLayout.getValue();

            if (layoutName.equals(parent.layoutDefault) || layoutName.equals(parent.layoutWhite))
            {
                if (lastColor < 0 || lastColor > 255)
                {
                    lastColor = 255;
                }

                if (preview_img != null)
                    preview_img.setBackgroundColor(Color.TRANSPARENT);
                setSummary((lastColor * 100 / 255) + "%");
            }
            else
            {
                lastColor = (lastColor >= 0 && lastColor <= 255) ? Constants.DEFAULT_BACKGROUND_COLOR : lastColor;

                // 如果有背景图，优先显示
                if (parent.backBitmap != null)
                {
                    if (preview_img != null)
                        preview_img.setBackgroundColor(Color.TRANSPARENT);
                    setSummary(R.string.custom_image);
                }
                else
                {
                    if (preview_img != null)
                        preview_img.setBackgroundColor(lastColor);
                    setSummary("#" + (Integer.toHexString(lastColor) + "").toUpperCase());
                }
            }
        }
    }

    public int getLastColor()
    {
        return lastColor;
    }

    public void setLastColor(int color)
    {
        lastColor = color;
        updateView();
    }

}
