package alei.switchpro.flash;

import alei.switchpro.MyApplication;
import alei.switchpro.R;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FlashlightUtils {
    private static View view;
    private static FlashlightCallback callback;

    private static boolean isEnabled = false;
    private static Object iHardwareService;
    private static Camera mCamera = null;

    static {
        iHardwareService = getHardwareService();
    }

    private static Object getHardwareService() {
        try {
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);
            Object hardwareService = getServiceMethod.invoke(null, "hardware");
            Class<?> iHardwareServiceStubClass = Class.forName("android.os.IHardwareService$Stub");
            Method asInterfaceMethod = iHardwareServiceStubClass.getMethod("asInterface", IBinder.class);
            return asInterfaceMethod.invoke(null, hardwareService);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setFlashlightDroid2(boolean b) {
        try {
            // 第一次调用获取不到参数则没打开闪光灯，返回false
            iHardwareService.getClass().getMethod("setFlashlightEnabled", boolean.class).invoke(iHardwareService, b);
        } catch (Exception e) {
            Log.d("setFlashlightDroid2", "fail");
        }

        isEnabled = b;
    }

    public static void setFlashlightDroid(Context context, boolean b) {
        try {
            Vibrator localVibrator = (Vibrator) context.getSystemService("vibrator");
            Field localField = Class.forName(localVibrator.getClass().getName()).getDeclaredField("mService");
            localField.setAccessible(true);
            Object obj = localField.get(localVibrator);
            Method setFlashlightEnabled = obj.getClass().getMethod("setFlashlightEnabled",
                    new Class[]{Boolean.class});
            setFlashlightEnabled.invoke(obj, b);
        } catch (Exception e) {
            Log.d("setFlashlightDroid", "fail");
        }

        isEnabled = b;
    }

    public static void setFlashlightHtc(boolean b) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter("/sys/devices/platform/flashlight.0/leds/flashlight/brightness"));

            if (b) {
                bw.write("125");
            } else {
                bw.write("0");
            }

            bw.close();
        } catch (Exception e) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e1) {
                }
            }

            Log.d("setFlashlightHtc", "fail");
        }

        isEnabled = b;
    }

    /**
     * private static final String TORCH_OFF = "0"; private static final String
     * TORCH_ON = "1";
     *
     * @param b
     */
    public static void setFlashlightSamsung(boolean b) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter("/sys/class/timed_output/flash/enable"));

            if (b) {
                bw.write("0");
            } else {
                bw.write("1");
            }

            bw.close();
        } catch (Exception e) {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e1) {
                }
            }
            Log.d("methodSamsung", "fail");
        }

        isEnabled = b;
    }

    public static void setFlashlightLG(boolean b) {
        int mode = VERSION.SDK_INT < 5 ? 0 : 1;

        File existFile = null;
        File file1 = new File("/sys/bus/i2c/devices/3-0028/flash");

        if (file1.exists() && file1.canWrite()) {
            existFile = file1;
        } else {
            existFile = new File("/sys/devices/platform/i2c-gpio.3/i2c-adapter/i2c-3/3-0028/flash");
        }

        if (b) {
            if (mCamera == null) {
                mCamera = Camera.open();
            }

            try {
                FileOutputStream outputStream = new FileOutputStream(existFile);

                if (mode == 1) {
                    outputStream.write(new byte[]{49, 48, 48});
                    mCamera.startPreview();
                } else {
                    outputStream.write(new byte[]{49});
                }

                outputStream.close();
            } catch (Exception e) {
                if (mCamera != null) {
                    if (mode == 1) {
                        mCamera.stopPreview();
                    }

                    mCamera.release();
                    mCamera = null;
                }
            }
        } else {
            if (mCamera != null) {
                if (mode == 1) {
                    mCamera.stopPreview();
                }

                mCamera.release();
                mCamera = null;
            }
        }

        isEnabled = b;
    }

    public static void setFlashlightV9(boolean b, Context paramContext) {
        if (b) {
            if (mCamera == null) {
                mCamera = Camera.open();
            }

            Camera.Parameters parameter = mCamera.getParameters();
            setFlashMode(parameter, "torch");
            mCamera.setParameters(parameter);
            mCamera.startPreview();
        } else {
            if (mCamera != null) {
                try {
                    Camera.Parameters parameter = mCamera.getParameters();
                    setFlashMode(parameter, "off");
                    mCamera.setParameters(parameter);
                    mCamera.stopPreview();
                } finally {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }

        isEnabled = b;
    }

    public static void setFlashlight(boolean b, Context paramContext) {
        if (b) {
            if (mCamera == null) {
                mCamera = Camera.open();
            }

            Camera.Parameters parameter = mCamera.getParameters();

            if (Build.MODEL.startsWith("GT-P1")) {
                setFlashMode(parameter, "on");
                mCamera.setParameters(parameter);
                mCamera.startPreview();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                    }
                });
            } else if (Build.MODEL.startsWith("LT15i")) {
                setFlashMode(parameter, "torch");
                mCamera.setParameters(parameter);
                SurfaceHolder surfaceHolder = new SurfaceView(paramContext).getHolder();
                surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2,
                                               int paramInt3) {
                    }

                    public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
                    }

                    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
                        mCamera.release();
                    }
                });
                surfaceHolder.setType(3);

                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                } catch (IOException localIOException) {
                    mCamera = null;
                }
            } else {
                setFlashMode(parameter, "torch");
                mCamera.setParameters(parameter);
            }
        } else {
            if (mCamera != null) {
                try {
                    Camera.Parameters parameter = mCamera.getParameters();
                    setFlashMode(parameter, "off");
                    mCamera.setParameters(parameter);
                } finally {
                    mCamera.release();
                    mCamera = null;
                }
            }
        }

        isEnabled = b;
    }

    public static boolean getFlashlight() {
        return isEnabled;
    }

    private static void setFlashMode(Parameters params, String mode) {
        try {
            params.getClass().getMethod("setFlashMode", new Class[]{String.class})
                    .invoke(params, new Object[]{mode});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setFlashlightDefault(boolean state, Context context) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            WindowManager wm = (WindowManager) context.getSystemService("window");

            if (state) {
                view = inflater.inflate(R.layout.view_camera, null);
                wm.addView(view, MyApplication.getInstance().getWindowParams());
                callback = new FlashlightCallback();
                SurfaceHolder surfaceHolder = ((SurfaceView) view.findViewById(R.id.preview)).getHolder();
                surfaceHolder.addCallback(callback);
            } else {
                wm.removeView(view);

                if (callback != null) {
                    callback.cleanUp();
                }
            }
        } catch (Exception ex) {
        }

        isEnabled = state;
    }

    /***
     * Attempts to set camera flash torch/flashlight mode on/off
     *
     * @param isOn true = on, false = off
     * @return boolean whether or not we were able to set it
     */
    public boolean setXperiaArcFlashlight(boolean isOn) {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters params = mCamera.getParameters();
        String value;

        if (isOn) // we are being ask to turn it on
        {
            value = Camera.Parameters.FLASH_MODE_TORCH;
        } else
        // we are being asked to turn it off
        {
            value = Camera.Parameters.FLASH_MODE_AUTO;
        }

        try {
            params.setFlashMode(value);
            mCamera.setParameters(params);

            String nowMode = mCamera.getParameters().getFlashMode();

            if (isOn && nowMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                return true;
            }
            if (!isOn && nowMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                return true;
            }
        } catch (Exception ex) {
        }

        return false;
    }
}
