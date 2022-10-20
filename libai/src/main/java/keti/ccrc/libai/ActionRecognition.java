package keti.ccrc.libai;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import java.io.IOException;
import java.util.List;

import keti.ccrc.libai.common.vision.CameraSource;
import keti.ccrc.libai.common.vision.CameraSourcePreview;
import keti.ccrc.libai.common.vision.GraphicOverlay;
import keti.ccrc.libai.common.vision.posedetector.PoseDetectorProcessor;

public class ActionRecognition {

    private static final String TAG = "ActionRecognition";

    static Context context;
    static Activity activity;

    static private CameraSourcePreview preview;
    static private GraphicOverlay graphicOverlay;
    static protected CameraSource cameraSource;

    static private boolean emulation;

    public static void init(Context c, boolean emul) {

        emulation = emul;

        if (c == null)
        {
            Toast.makeText(c,"Error: no context specified", Toast.LENGTH_SHORT).show();
            return;
        }

        context = c;
        activity = getActivity(context);

        int previewResourceId = activity.getResources().getIdentifier("camera_source_preview", "id", activity.getPackageName());
        int overlayResourceId = activity.getResources().getIdentifier("graphic_overlay", "id", activity.getPackageName());


        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(c,"Error: camera permission required",Toast.LENGTH_SHORT).show();
            return;
        }

        //preview = activity.findViewById(R.id.camera_source_preview);
        //graphicOverlay = activity.findViewById(R.id.graphic_overlay);
        preview = activity.findViewById(previewResourceId);
        graphicOverlay = activity.findViewById(overlayResourceId);

        if (cameraSource == null) {
            cameraSource = new CameraSource(activity, graphicOverlay, emulation);
        }
        setProcessor();
    }

    private static void setProcessor() {
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build();
        cameraSource.setMachineLearningFrameProcessor(new PoseDetectorProcessor(
                        context,
                        options,
                        true,
                        false,
                        false,
                        true,
                        true
                )
        );
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    public static void start() {

        if (cameraSource == null) {
            cameraSource = new CameraSource(activity, graphicOverlay, emulation);
            setProcessor();
        }

        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    public static List<String> getCurrentAction() {
        return PoseDetectorProcessor.getActivityResult();
    }

    public static void stop() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;

            graphicOverlay.clear();
        }
    }

    private static Activity getActivity(Context context)
    {
        if (context == null)
        {
            return null;
        }
        else if (context instanceof ContextWrapper)
        {
            if (context instanceof Activity)
            {
                return (Activity) context;
            }
            else
            {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }

        return null;
    }
}
