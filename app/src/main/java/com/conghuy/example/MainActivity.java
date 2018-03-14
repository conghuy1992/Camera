package com.conghuy.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.conghuy.example.adapters.EffectsAdapter;
import com.conghuy.example.adapters.SpinnerResolutionAdapter;
import com.conghuy.example.cameras.CameraPreview;
import com.conghuy.example.classs.Const;
import com.conghuy.example.classs.PrefManager;
import com.conghuy.example.customUI.VideoDialog;
import com.conghuy.example.dtos.Effects;
import com.conghuy.example.interfaces.DetectDialogDissmiss;
import com.conghuy.example.interfaces.EffectCallBack;
import com.conghuy.example.interfaces.Statics;

public class MainActivity extends Activity implements OnClickListener {
    private String TAG = "MainActivity";
    private Camera mCamera;
    private CameraPreview mPreview;
    private PictureCallback mPicture;
    private ImageView ivSwitch;
    private ImageView capture;
    private Context myContext;
    private LinearLayout cameraPreview;

    private boolean cameraFront = false;
    private Spinner spinnerResolution;
    private SpinnerResolutionAdapter adapter;
    private ImageView ivFlash;

    private RecyclerView recyclerViewColorEffect;
    private ImageView ivColorEffect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        cameraIdListener = cameraId;
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        cameraIdListener = cameraId;
        return cameraId;
    }

    private OrientationEventListener cOrientationEventListener;
    private int cameraIdListener;

    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            int cameraId = 0;
            Log.d(TAG, "cameraFront:" + cameraFront);
            if (cameraFront) {
                // continue open front camera
                cameraId = findFrontFacingCamera();
                // No front facing camera found.
                if (cameraId < 0) {
                    Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                    ivSwitch.setVisibility(View.GONE);
                    cameraId = findBackFacingCamera();
                }
            } else {
                // continue open back camera
                cameraId = findBackFacingCamera();
            }
            refreshCamera(cameraId);
        }
    }

    public Camera.CameraInfo getCameraInfo(int CAMERA_ID) {
        Camera.CameraInfo cameraInfo = null;

        if (mCamera != null) {
            // Get camera info only if the camera is available
            cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(CAMERA_ID, cameraInfo);
        }

        if (mCamera == null || cameraInfo == null) {
            // Camera is not available, display error message
            Toast.makeText(myContext, "Camera is not available.", Toast.LENGTH_SHORT).show();
            finish();
        }
        return cameraInfo;
    }

    private void setImageFlash(boolean flag) {
        if (ivFlash != null)
            ivFlash.setImageResource(flag ? R.drawable.ic_flash_auto_white_24dp : R.drawable.ic_flash_off_white_24dp);
    }

    public void initialize() {
        ivColorEffect = (ImageView) findViewById(R.id.ivColorEffect);
        ivColorEffect.setOnClickListener(this);
        ivFlash = (ImageView) findViewById(R.id.ivFlash);
        ivFlash.setOnClickListener(this);
        setImageFlash(new PrefManager(myContext).isFlash());

        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);

        // Get the rotation of the screen to adjust the preview image accordingly.
        final int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
        mPreview = new CameraPreview(myContext, mCamera, displayRotation);
        cameraPreview.addView(mPreview);


        capture = (ImageView) findViewById(R.id.button_capture);
        capture.setOnClickListener(this);

        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);

    }

    private void switchCamera() {
        //get the number of cameras
        int camerasNumber = Camera.getNumberOfCameras();
        if (camerasNumber > 1) {
            //release the old camera instance
            //switch camera, from the front and the back and vice versa

            releaseCamera();
            chooseCamera();
        } else {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void chooseCamera() {
        //if the camera preview is the front
        int cameraId;
        if (cameraFront) {
            cameraId = findBackFacingCamera();
            if (cameraId >= 0) {

            } else {
                Log.d(TAG, "chooseCamera findBackFacingCamera null");
                cameraId = findFrontFacingCamera();
            }
        } else {
            cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {

            } else {
                Log.d(TAG, "chooseCamera findFrontFacingCamera null");
                cameraId = findBackFacingCamera();
            }
        }
        if (cameraId < 0) {
            Const.showMsg(myContext, R.string.no_faceing_camera);
            finish();
        }
        refreshCamera(cameraId);
    }

    private void setFlashCamera(boolean flag) {
        Camera.Parameters params = mCamera.getParameters();
        if (flag) params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        else params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }

    private void refreshCamera(final int cameraId) {
        mCamera = Camera.open(cameraId);

        final Camera.Parameters params = mCamera.getParameters();

        List<String> stringList = params.getSupportedColorEffects();
        recyclerViewColorEffect = (RecyclerView) findViewById(R.id.recyclerViewColorEffect);

        if (stringList != null && stringList.size() > 0) {
            List<Effects> effectsList = new ArrayList<>();

            for (int i = 0; i < stringList.size(); i++) {
                boolean flag = false;
                if (i == 0) {
                    flag = true;
                }
                Effects effects = new Effects(stringList.get(i), flag);
                effectsList.add(effects);
            }
            EffectsAdapter effectsAdapter = new EffectsAdapter(myContext, effectsList, new EffectCallBack() {
                @Override
                public void onComplete(String value) {
                    Log.d(TAG, "value:" + value);
                    Camera.Parameters params = mCamera.getParameters();
                    params.setColorEffect(value);
                    mCamera.setParameters(params);
                }
            });

            recyclerViewColorEffect.setLayoutManager(new LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false));
            recyclerViewColorEffect.setAdapter(effectsAdapter);
        } else {

        }

        // Check what resolutions are supported by your camera
        final List<Camera.Size> sizes = params.getSupportedPictureSizes();
//        for (Camera.Size size : sizes) {
//            Log.i(TAG, "Available resolution: " + size.width + " " + size.height);
//        }

        spinnerResolution = (Spinner) findViewById(R.id.spinnerResolution);

        if (sizes != null && sizes.size() > 0) {
            // spinnerResolution
            spinnerResolution.setVisibility(View.VISIBLE);
            adapter = new SpinnerResolutionAdapter(myContext, sizes);
            spinnerResolution.setAdapter(adapter);
            spinnerResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    Log.d(TAG, "position:" + position);
                    // setParameters
                    Camera.Size size = sizes.get(position);
                    params.setPictureSize(size.width, size.height);

                    if (cameraId == Const.getFrontFacingCamera()) {

                    } else {
                        // setFlashMode
                        if (new PrefManager(myContext).isFlash())
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        else params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                        params.setExposureCompensation(0);
                    }

                    params.setColorEffect(Camera.Parameters.EFFECT_NONE);
                    params.setJpegQuality(100);

                    // setParameters
                    mCamera.setParameters(params);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            spinnerResolution.setVisibility(View.GONE);
        }

        mPicture = getPictureCallback();
        previewRefreshCamera(mCamera, cameraId);

        if (cOrientationEventListener == null) {
            cOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                public void onOrientationChanged(int orientation) {
//                        Log.d(TAG, "orientation:" + orientation);
                    if (orientation == ORIENTATION_UNKNOWN) return;
                    Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                    android.hardware.Camera.getCameraInfo(cameraIdListener, info);
                    orientation = (orientation + 45) / 90 * 90;
                    int rotation = 0;
                    if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                        rotation = (info.orientation - orientation + 360) % 360;
                    } else {  // back-facing camera
                        rotation = (info.orientation + orientation) % 360;
                    }
                    Camera.Parameters params = mCamera.getParameters();
                    params.setRotation(rotation);
                    mCamera.setParameters(params);
                }
            };
        }
        if (cOrientationEventListener.canDetectOrientation()) {
            cOrientationEventListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private PictureCallback getPictureCallback() {
        PictureCallback picture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //make a new picture file
                File pictureFile = getOutputMediaFile(Statics.MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    return;
                }
                try {
                    //write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + Statics.FOLDER + "/" + pictureFile.getName();
                    Const.showMsg(myContext, path);

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
                //refresh camera to continue preview
                previewRefreshCamera(mCamera, cameraIdListener);
            }
        };
        return picture;
    }

    private void previewRefreshCamera(Camera mCamera, int cameraId) {
        mPreview.refreshCamera(mCamera, getCameraInfo(cameraId));
    }

    //make picture and save to a folder
    private static File getOutputMediaFile(int type) {

        // save to Picture/JCG Camera/...
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Statics.FOLDER);

        //make a new file directory inside the "sdcard" folder
//        File mediaStorageDir = new File("/sdcard/", Statics.FOLDER);

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        String timeStamp = Const.getTimeStamp();
        File mediaFile;
        // Create a media file name
//        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        if (type == Statics.MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + Statics.JPG);
        } else if (type == Statics.MEDIA_TYPE_VIDEO) {
            mediaFile = new File(Const.getPathFile(mediaStorageDir, timeStamp));
        } else {
            return null;
        }
        return mediaFile;
    }

    private void releaseCamera() {
        releaseMediaRecorder();
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        // // stop and release cOrientationEventListener
        if (cOrientationEventListener != null) {
            cOrientationEventListener.disable();
            cOrientationEventListener = null;
        }

    }

    private void setFlash() {
        boolean isFlash = new PrefManager(myContext).isFlash();
        boolean flag = isFlash ? false : true;
        new PrefManager(myContext).setFlash(flag);
        setImageFlash(flag);
        setFlashCamera(flag);
    }

    boolean isMenuColorEffect = true;

    private void showMenuColorEffect() {
        if (recyclerViewColorEffect != null) recyclerViewColorEffect.setVisibility(View.VISIBLE);
    }

    private void hideMenuColorEffect() {
        if (recyclerViewColorEffect != null) recyclerViewColorEffect.setVisibility(View.GONE);
    }

    private void setIvColorEffectBackground(boolean flag) {
        if (ivColorEffect != null)
            ivColorEffect.setImageResource(flag ? R.drawable.ic_expand_more_white_36dp : R.drawable.ic_expand_less_white_36dp);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the pointer ID
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            int action = event.getAction();
            if (event.getPointerCount() > 1) {
                // handle multi-touch events
                if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    mDist = getFingerSpacing(event);
                } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                    mCamera.cancelAutoFocus();
                    handleZoom(event, params);
                }
            } else {
                // handle single touch events
                if (action == MotionEvent.ACTION_UP) {
                    handleFocus(event, params);
                }
            }
        }
        return true;
    }

    private float mDist;

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });
        }
    }

    /**
     * Determine the space between the first two fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private MediaRecorder mMediaRecorder;
    private String strOutputFile = "";

    private boolean prepareVideoRecorder() {
        try {
            mMediaRecorder = new MediaRecorder();


            // Step 1: Unlock and set camera to MediaRecorder
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);

            // Step 2: Set sources
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

            // Step 4: Set output file
            strOutputFile = getOutputMediaFile(Statics.MEDIA_TYPE_VIDEO).toString();
            Log.d(TAG, "strOutputFile:" + strOutputFile);
            mMediaRecorder.setOutputFile(strOutputFile);

            // Step 5: Set the preview output
            mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

            // Step 6: Prepare configured MediaRecorder
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private VideoDialog videoDialog;

    private void releaseMediaRecorder() {

        Log.d(TAG, "releaseMediaRecorder");
        if (mMediaRecorder != null) {
            Log.d(TAG, "mMediaRecorder != null");
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
        if (!isRecording && strOutputFile.length() > 0) {
            if (videoDialog != null && videoDialog.isShowing()) videoDialog.dismiss();
            videoDialog = new VideoDialog(myContext, strOutputFile, 3);
            videoDialog.show();
        }
        strOutputFile = "";
        isRecording = true;

    }

    boolean isRecording = true;

    void startCapture() {

//            mCamera.takePicture(null, null, mPicture);//capture

        // initialize video camera
        if (isRecording) {
            prepareVideoRecorder();
            // now you can start recording
            mMediaRecorder.start();
            // inform the user that recording has started
//            setCaptureButtonText("Stop");
            isRecording = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isRecording) {
                        releaseMediaRecorder();
                    }
                }
            }, 1000);
        } else {
            // prepare didn't work, release the camera
            releaseMediaRecorder();

            // inform user
        }
    }

    @Override
    public void onClick(View v) {
        if (v == ivFlash) {
            setFlash();
        } else if (v == capture) {
            startCapture();
        } else if (v == ivSwitch) {
            switchCamera();
        } else if (v == ivColorEffect) {
            setIvColorEffectBackground(isMenuColorEffect);
            if (isMenuColorEffect) {
                isMenuColorEffect = false;
                showMenuColorEffect();
            } else {
                isMenuColorEffect = true;
                hideMenuColorEffect();
            }
        }
    }
}