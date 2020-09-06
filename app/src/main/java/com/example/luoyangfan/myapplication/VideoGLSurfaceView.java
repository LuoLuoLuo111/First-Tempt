package com.example.luoyangfan.myapplication;

import android.content.Context;
import android.graphics.PixelFormat;
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
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.setZOrderOnTop(true);
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
                 if(mOnRenderGreatedListener  != null){
                     mOnRenderGreatedListener.onRenderCreated(surface);
                 }
            }
        });
    }

    public VideoRender getRender(){
        return mRender;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private OnRenderGreatedListener mOnRenderGreatedListener;

    public void setRenderCreated(OnRenderGreatedListener l){
        mOnRenderGreatedListener = l;
    }

    public interface OnRenderGreatedListener{
        void onRenderCreated(Surface surface);
    }
}
