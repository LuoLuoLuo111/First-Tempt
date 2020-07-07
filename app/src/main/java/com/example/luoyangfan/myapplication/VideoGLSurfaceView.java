package com.example.luoyangfan.myapplication;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Surface;

public class VideoGLSurfaceView extends GLSurfaceView {

    private Context mContext;
    private VideoRender mRender;
    public VideoGLSurfaceView(Context context) {
        super(context, null);
    }

    public VideoGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setEGLContextClientVersion(2);
        mRender = new VideoRender(context);
        setRenderer(mRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mRender.setmOnRenderListener(new VideoRender.OnRenderListener() {
            @Override
            public void onRender() {
                requestRender();
            }
        });
        mRender.setmOnSurfaceCreateListener(new VideoRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(Surface surface) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Camera/视频/VID_20200618_040456.mp4";
                VideoDecode decode = new VideoDecode(path, surface);
                decode.prepare();
            }
        });
    }

    public VideoRender getRender(){
        return mRender;
    }


}
