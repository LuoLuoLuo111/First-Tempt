package com.example.luoyangfan.myapplication;

import android.nfc.Tag;
import android.util.Log;
import android.view.Surface;

public class VideoModel implements VideoDecode.OnPtsListener {
    private final static String TAG = "VideoModel";

    private VideoDecode mVideoDecode;

    private long mDurations;

    public VideoModel(String path, Surface surface){
        mVideoDecode = new VideoDecode(path, surface);
        mVideoDecode.setmOnPtsListener(this);
        mDurations = mVideoDecode.getDuration()/1000;
        Log.v(TAG,"VideoModel duration = "+mDurations);
    }



    public void prepare(){
        if(mVideoDecode != null){
            mVideoDecode.prepare();
        }
    }

    public void setPtsListener(VideoDecode.OnPtsListener l){
        if(mVideoDecode != null){
            mVideoDecode.setmOnPtsListener(l);
        }
    }

    public void play(){
        if(mVideoDecode != null){
            mVideoDecode.play();
        }
    }

    public void pause(){
        if(mVideoDecode != null){
            mVideoDecode.pause();
        }
    }

    @Override
    public void onPtsUpdate(long ptsMs) {
        Log.v(TAG,"onPtsUpdate = "+ptsMs);
        if(mOnModelPTSUpdateListener != null){
            mOnModelPTSUpdateListener.onModelPTSUpdate(ptsMs/1000, (float)ptsMs/1000/mDurations);
        }
    }

    private OnModelPTSUpdateListener mOnModelPTSUpdateListener;

    public void setOnModePTSUpdateListener(OnModelPTSUpdateListener l){
        mOnModelPTSUpdateListener = l;
    }

    public interface OnModelPTSUpdateListener{
        void onModelPTSUpdate(long pts, float ptsRatio);
    }
}
