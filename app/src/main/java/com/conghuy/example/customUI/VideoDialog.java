package com.conghuy.example.customUI;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.conghuy.example.R;
import com.conghuy.example.classs.Const;
import com.conghuy.example.interfaces.DetectDialogDissmiss;
import com.conghuy.example.interfaces.MergerVideoCallBack;

/**
 * Created by maidinh on 06-Oct-17.
 */

public class VideoDialog extends Dialog implements View.OnClickListener {
    private String TAG = "VideoDialog";
    private String path;
    private int count;
    private ProgressBar progressBar;
    private Context context;
    private VideoView videoView;
    private FrameLayout ivClose;
    private FrameLayout ivShare;
    private FrameLayout ivInfo;
    private FrameLayout ivDelete;


    public VideoDialog(@NonNull Context context, String path, int count) {
        super(context);
        this.context = context;
        this.path = path;
        this.count = count;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.video_dialog_layout);

        init();

        // setOnKeyListener dialog
//        this.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    Log.d("audioPlayer", "KEYCODE_BACK");
//
//                }
//                return true;
//            }
//        });

        // getWindow
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    void hideProgressBar() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void success(String pathFile) {
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse(pathFile); //Declare your url here.

        VideoView mVideoView = (VideoView) findViewById(R.id.videoView);
        // Start the MediaController
        MediaController mediacontroller = new MediaController(context);
        mediacontroller.setAnchorView(mVideoView);
        mVideoView.setMediaController(mediacontroller);
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.start();
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
    }

    private void init() {
        ivClose = (FrameLayout) findViewById(R.id.ivClose);
        ivClose.setOnClickListener(this);
        ivShare = (FrameLayout) findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivInfo = (FrameLayout) findViewById(R.id.ivInfo);
        ivInfo.setOnClickListener(this);
        ivDelete = (FrameLayout) findViewById(R.id.ivDelete);
        ivDelete.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final Const con = new Const();
        con.new MergerVideo(context, path, count, new MergerVideoCallBack() {
            @Override
            public void onSuccess(String pathFile) {
                hideProgressBar();
                Log.d(TAG, "pathFile:" + pathFile);
                success(pathFile);
            }

            @Override
            public void onFail() {
                hideProgressBar();
                Const.showMsg(context, R.string.error);
            }
        }).execute();
    }

    @Override
    public void onClick(View v) {
        if (v == ivClose) {
            dismiss();
        } else if (v == ivShare) {
        } else if (v == ivInfo) {
        } else if (v == ivDelete) {
        }
    }
}
