package alei.switchpro.icon;

import alei.switchpro.Constants;
import alei.switchpro.R;
import alei.switchpro.WidgetProviderUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class IconPreferenceScreen extends Preference
{
    private Drawable mIcon;
    private int iconId;
    private Context context;

    public IconPreferenceScreen(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.context = context;
        setLayoutResource(R.layout.view_pref_image_button);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconPreferenceScreen, defStyle, 0);
        mIcon = a.getDrawable(R.styleable.IconPreferenceScreen_customicon);
        iconId = a.getInt(R.styleable.IconPreferenceScreen_iconid, 0);
    }

    public IconPreferenceScreen(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    @Override
    public void onBindView(View view)
    {
        super.onBindView(view);
        final ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        final Button button = (Button) view.findViewById(R.id.btn);
        final SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        final String fileName = config.getString(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, iconId),
                "1_icon.png");

        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View paramView)
            {
                // 能点击，说明文件和配置已经存在
                config.edit().remove(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, iconId)).commit();
                context.deleteFile(fileName);
                button.setEnabled(false);
                WidgetProviderUtil.freeMemory(iconId);

                if (imageView != null && mIcon != null)
                {
                    // 清除滤镜
                    mIcon.clearColorFilter();
                    mIcon.setAlpha(255);
                    imageView.setImageDrawable(mIcon);
                }
            }
        });

        if (config.contains(String.format(Constants.PREFS_CUSICON_FIELD_PATTERN, iconId)))
        {
            button.setEnabled(true);

            if (imageView != null && mIcon != null)
            {
                try
                {
                    BitmapDrawable cusIcon = new BitmapDrawable(BitmapFactory.decodeStream(context
                            .openFileInput(fileName)));
                    cusIcon.clearColorFilter();
                    cusIcon.setAlpha(255);
                    imageView.setImageDrawable(cusIcon);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            button.setEnabled(false);

            if (imageView != null && mIcon != null)
            {
                // 清除滤镜
                mIcon.clearColorFilter();
                mIcon.setAlpha(255);
                imageView.setImageDrawable(mIcon);
            }
        }
    }

    public void update()
    {
        notifyChanged();
    }

}
