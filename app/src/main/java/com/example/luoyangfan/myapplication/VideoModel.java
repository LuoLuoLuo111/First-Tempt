package com.example.luoyangfan.myapplication;

import android.view.Surface;

public class VideoModel {

    private VideoDecode mVideoDecode;

    public VideoModel(String path, Surface surface){
        mVideoDecode = new VideoDecode(path, surface);

    }



    public void prepare(){
        if(mVideoDecode != null){
            mVideoDecode.prepare();
        }
    }
    

    public void play(){

    }

    public void pause(){
        
    }

}
