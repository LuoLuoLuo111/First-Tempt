package com.example.luoyangfan.myapplication;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.jar.Manifest;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements VideoGLSurfaceView.OnRenderGreatedListener, VideoModel.OnModelPTSUpdateListener {
    private final static String TAG = "MainActivity";

    private final static int MSG_PTSUPDATE = 1;

    private VideoGLSurfaceView mVideoGLSurfaceView;
    private Button button1, button2, button3, button4;
    private SeekBar mSeekBar;

    private VideoModel mVideoModel;
    private MainHandler mMainHandler;

    private final static String WRITEPERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final static String READPERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE;
    String[] permissions={
        WRITEPERMISSION,
            READPERMISSION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"onCreate");

        setContentView(R.layout.activity_main);
        mVideoGLSurfaceView = (VideoGLSurfaceView) findViewById(R.id.video_glsurfaceview);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        ViewGroup.LayoutParams lp = mVideoGLSurfaceView.getLayoutParams();
        lp.width = 1080;
        lp.height = 498;
        mVideoGLSurfaceView.setLayoutParams(lp);
        mVideoGLSurfaceView.setRenderCreated(this);
        mMainHandler = new MainHandler(this);
        TextView tegetherDayView = (TextView)findViewById(R.id.our_together_day);
        tegetherDayView.setText(""+daysFromOurTogether());
    }

    protected void onResume(){
        super.onResume();
        Log.v(TAG,"onResume");
        String pathDir = Environment.getExternalStorageDirectory().getAbsolutePath()+"/bitmap/";
        Log.v(TAG,"pathDir = "+pathDir);
        File file = new File(pathDir);
        if(!file.exists()){
            Boolean b = file.mkdirs();
            Log.v(TAG,"b = "+b);
        }

        requestPermissions();
    }


    private int daysFromOurTogether(){
        Calendar togetherDay = Calendar.getInstance();
        togetherDay.set(2017,6,1);
        Calendar today = Calendar.getInstance();
        long time = today.getTimeInMillis()-togetherDay.getTimeInMillis();
        long oneDay = 24*60*60*1000;
        return  (int)(time/oneDay)+1;

    }

    private void requestPermissions(){
        if(checkPermission(READPERMISSION) && checkPermission(WRITEPERMISSION)){
            Log.v(TAG,"requestPermissions had permissions");
        }else{
            Log.w(TAG,"requestPermissions !!!");
            this.requestPermissions(permissions,0);
        }
    }

    private boolean checkPermission(String permission){
        Log.w(TAG,"checkPermission ");
         if(this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED){
             Log.w(TAG,"checkPermission : true");
             return true;
         }else{
             Log.w(TAG,"checkPermission : false");
             return  false;
         }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG,"permissions.length = "+permissions.length);
        if(grantResults.length>=1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.w(TAG,"onRequestPermissionsResult : ok");
            }else{
                Log.w(TAG,"onRequestPermissionsResult : nonono");
            }
            if(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Log.w(TAG,"onRequestPermissionsResult : ok");
            }else{
                Log.w(TAG,"onRequestPermissionsResult : nonono");
            }
        }

    }

    @Override
    public void onRenderCreated(Surface surface) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tencent/MicroMsg/WeiXin/wx_camera_1595249231978.mp4";
        mVideoModel = new VideoModel(path, surface);
        mVideoModel.prepare();
        mVideoModel.setOnModePTSUpdateListener(this);
    }

    private static class MainHandler extends Handler{

        private final WeakReference<MainActivity> mMainActivity;

        MainHandler(MainActivity activity){
            mMainActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(mMainActivity.get() == null) return;
            switch (msg.what){
                case MSG_PTSUPDATE:
                    Log.v(TAG,"MSG_PTSUPDATE : "+msg.arg1+";"+(float)msg.obj);
                    float ratio = (float) msg.obj;
                    mMainActivity.get().mSeekBar.setProgress((int)(100 * ratio));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onModelPTSUpdate(long pts, float ptsRatio) {
         if(mSeekBar != null){
             Message m = mMainHandler.obtainMessage(MSG_PTSUPDATE);
             m.arg1 = (int)pts;
             m.obj = (Float)ptsRatio;
             mMainHandler.sendMessage(m);
         }
    }
}
