package com.conghuy.example.classs;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.Toast;

import com.conghuy.example.interfaces.MergerVideoCallBack;
import com.conghuy.example.interfaces.Statics;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by maidinh on 04-Oct-17.
 */

public class Const {
    public static String TAG = "Const";

    public static int calculatePreviewOrientation(Camera.CameraInfo info, int rotation, Camera mCamera) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Log.d(TAG, "degrees:" + degrees);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

//        Camera.Parameters params = mCamera.getParameters();
//        params.setRotation(result);
//        mCamera.setParameters(params);
        return result;
    }

    public static int getFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static void animaRotation(ImageView iv, int value) {
        iv.animate().rotation(value);
    }

    public static String getMsg(Context context, int id) {
        return context.getResources().getString(id);
    }

    public static void showMsg(Context context, int id) {
        Toast.makeText(context, getMsg(context, id), Toast.LENGTH_SHORT).show();
    }

    public static void showMsg(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static int getColor(Context context, int id) {
        return context.getResources().getColor(id);
    }

    public class MergerVideo extends AsyncTask<String, String, String> {
        private Context context;
        private String path;
        private int count;
        private MergerVideoCallBack callBack;

        public MergerVideo(Context context, String path, int count, MergerVideoCallBack callBack) {
            this.context = context;
            this.path = path;
            this.count = count;
            this.callBack = callBack;
        }

        @Override
        protected String doInBackground(String... strings) {
            return merger(context, path, count);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.length() > 0) callBack.onSuccess(s);
            else callBack.onFail();
        }
    }

    public String merger(Context context, String path, int count) {
        MovieCreator mc = new MovieCreator();
        Movie video = null;
        try {
            video = mc.build(path);
            LinkedList<Track> videoTracks = new LinkedList<Track>();
            LinkedList<Track> audioTracks = new LinkedList<Track>();
            long[] audioDuration = {0}, videoDuration = {0};
            for (int i = 0; i < count; i++) {
                for (Track t : video.getTracks()) {
//                    if (t.getHandler().equals("soun")) {
//                        for (long a : t.getSampleDurations()) audioDuration[0] += a;
//                        audioTracks.add(t);
//                    } else
                    if (t.getHandler().equals("vide")) {
                        for (long v : t.getSampleDurations()) videoDuration[0] += v;
                        videoTracks.add(t);
                    }
                }
            }
            //Append all audio and video
            Movie outputMovie = new Movie();
            if (videoTracks.size() > 0)
                outputMovie.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));

//            if (audioTracks.size() > 0)
//                outputMovie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));

            Container out = new DefaultMp4Builder().build(outputMovie);

            // Create a media file name
            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Statics.FOLDER);
            String timeStamp = getTimeStamp();
            String filename = getPathFile(folder, timeStamp);

            FileChannel fc = new RandomAccessFile(String.format(filename), "rw").getChannel();
            out.writeContainer(fc);
            fc.close();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
            return filename;
        } catch (IOException e) {
            return "";
//            e.printStackTrace();
        }
    }

    public static String getPathFile(File mediaStorageDir, String timeStamp) {
        return mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + Statics.MP4;
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat(Statics.yyyyMMdd_HHmmss).format(new Date());
    }
}
