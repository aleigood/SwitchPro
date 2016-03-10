package alei.switchpro.flash;

import android.hardware.Camera;
import android.view.SurfaceHolder;

public class FlashlightCallback implements SurfaceHolder.Callback {
    private Camera camera;
    private Camera.Parameters cameraParameters;

    public void cleanUp() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            cameraParameters = null;
            camera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1, int paramInt2, int paramInt3) {
    }

    public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
        if (camera == null) {
            try {
                camera = Camera.open();
                cameraParameters = camera.getParameters();
                cameraParameters.setFocusMode("infinity");
                cameraParameters.setFlashMode("torch");
                camera.setParameters(cameraParameters);
                camera.startPreview();
                camera.setPreviewDisplay(paramSurfaceHolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
        cleanUp();
    }

}