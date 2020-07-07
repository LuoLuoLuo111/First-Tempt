package com.example.luoyangfan.myapplication;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class VideoDecode {

    private final static String TAG = "VideoDecode";

    private MediaExtractor mExtractor;
    private MediaCodec mDecoder;
    private MediaFormat mMediaFormat;

    private String mPath;

    public VideoDecode(String path, Surface surface){
        mPath = path;
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        int videoTrackIndex = getMediaTrackIndex("video/");
        MediaFormat mediaFormat = null;
        if(videoTrackIndex >= 0){
            mediaFormat = mExtractor.getTrackFormat(videoTrackIndex);
            mExtractor.selectTrack(videoTrackIndex);
        }
        try {
            mDecoder = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDecoder.configure(mMediaFormat, surface, null, 0);
    }

    public void prepare(){

        mDecoder.start();
        DecodeThread thread = new DecodeThread();
        thread.start();
    }

    private  class DecodeThread  extends Thread{

        @Override
        public void run() {
            MediaCodec.BufferInfo  mediaCodecInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
            boolean isEOS = false;
            long startMs = System.currentTimeMillis();
            while (true){
                if(!isEOS){
                    isEOS = putBufferToDecode(inputBuffers);
                }
                int outputIndex = mDecoder.dequeueOutputBuffer(mediaCodecInfo, -1);
                switch (outputIndex){
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.v(TAG,"format changed!");
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.v(TAG,"info try again later");
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.v(TAG,"info output buffers changed!");
                        break;

                    default:
                        mDecoder.releaseOutputBuffer(outputIndex, true);

                }
                sleepRender(mediaCodecInfo.presentationTimeUs, startMs);
                if(isEOS){
                    break;
                }

            }
            mDecoder.stop();
            mDecoder.release();
        }
    }

    private void sleepRender(long pts, long startMs){
        long ptsMs = pts/1000;
        long curMs = System.currentTimeMillis();
        if(curMs - startMs < ptsMs){

                Thread.yield();
                SystemClock.sleep(ptsMs - (curMs - startMs));

        }

    }

    private int getMediaTrackIndex(String media_type){
        int trackIndex =-1;
        for(int i = 0; i < mExtractor.getTrackCount(); i++){
            mMediaFormat = mExtractor.getTrackFormat(i);
            String mime = mMediaFormat.getString(MediaFormat.KEY_MIME);
            if(mime.startsWith(media_type)){
                trackIndex = i;
                break;
            }
        }
        return trackIndex;
    }

    private boolean putBufferToDecode(ByteBuffer[] inputBuffers){
        boolean isEos = false;
        int inputBufferIndex = mDecoder.dequeueInputBuffer(-1);
        if(inputBufferIndex >= 0 ){
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = mExtractor.readSampleData(inputBuffer, 0);
            if(sampleSize < 0 ){
                mDecoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEos = true;
            }else{
                mDecoder.queueInputBuffer(inputBufferIndex,0, sampleSize, mExtractor.getSampleTime(), 0);
                mExtractor.advance();
            }
        }
        return isEos;
    }


}
