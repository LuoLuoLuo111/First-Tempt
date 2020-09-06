package com.example.luoyangfan.myapplication;

import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
    private MediaExtractor mAudioExtractor;
    private MediaCodec mAudioDecoder;
    private MediaFormat mMediaFormat;
    private MediaFormat mAudioMediaFormat;
    private AudioTrack mAudioTrack;

    private String mPath;

    public VideoDecode(String path, Surface  surface){
        Log.v(TAG,"lyfnew  VideoDecode!");

        mPath = path;
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(mPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int videoTrackIndex = getMediaTrackIndex(mExtractor,"video/");
        if(videoTrackIndex >= 0){
            Log.v(TAG,"lyfnew  videoTrackIndex  = "+videoTrackIndex);

            mMediaFormat = mExtractor.getTrackFormat(videoTrackIndex);
            mExtractor.selectTrack(videoTrackIndex);
        }
        try {
            mDecoder = MediaCodec.createDecoderByType(mMediaFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDecoder.configure(mMediaFormat, surface, null, 0);

        mAudioExtractor = new MediaExtractor();
        try {
            mAudioExtractor.setDataSource(mPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int audioTrackIndex = getMediaTrackIndex(mAudioExtractor,"audio/");
        Log.v(TAG,"lyfnew  audioTrackIndex  = "+audioTrackIndex);
        if(audioTrackIndex >= 0){
            mAudioMediaFormat = mAudioExtractor.getTrackFormat(audioTrackIndex);
            mAudioExtractor.selectTrack(audioTrackIndex);
        }

        try {
            mAudioDecoder = MediaCodec.createDecoderByType(mAudioMediaFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAudioDecoder.configure(mAudioMediaFormat,null, null, 0);

        int audiochannels = mAudioMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int audioSampleRate = mAudioMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int maxInputSize = mAudioMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        int minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate, audiochannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        int audioInputBufferSize = minBufferSize > 0 ? minBufferSize * 4 : maxInputSize;
        int frameSizeInBytes = audiochannels * 2;   //这里一般是双声道，一帧两个声道，一个声道的一帧16bit的值，一共是所以是4字节
        audioInputBufferSize = (audioInputBufferSize / frameSizeInBytes) * frameSizeInBytes;
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioSampleRate, audiochannels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                         AudioFormat.ENCODING_PCM_16BIT, audioInputBufferSize,AudioTrack.MODE_STREAM);
    }

    public void prepare(){
        Log.v(TAG,"lyfnew  prepare!");

        mDecoder.start();
        mAudioDecoder.start();
        DecodeThread thread = new DecodeThread();
        AudioDecodeThread audioThread = new AudioDecodeThread();
        audioThread.start();
        thread.start();

        mAudioTrack.play();
    }

    private class AudioDecodeThread extends Thread{
        @Override
        public void run() {
            Log.v(TAG,"lyfnew  audio run in ");
            MediaCodec.BufferInfo mediaCodecInfo = new MediaCodec.BufferInfo();
            ByteBuffer[] inputBuffers = mAudioDecoder.getInputBuffers();
            ByteBuffer[] outputBuffers = mAudioDecoder.getOutputBuffers();
            boolean isEOS = false;
            long startMS = System.currentTimeMillis();
            Log.v(TAG,"lyfnew  audio run in 1");

            while (true){
                if(!isEOS){
                    isEOS = putBufferToAudioDecode(inputBuffers);
                }
                Log.v(TAG,"lyfnew  audio run in 2");
                int outputIndex = mAudioDecoder.dequeueOutputBuffer(mediaCodecInfo, 1000);
                Log.v(TAG,"lyfnew  audio run in 3");

                Log.v(TAG,"lyfnew  audio outputIndex = "+outputIndex);
                switch (outputIndex){
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                        Log.v(TAG,"format changed!");
                        MediaFormat format = mAudioDecoder.getOutputFormat();
                        mAudioTrack.setPlaybackRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                        break;
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        Log.v(TAG,"info try again later");
                        break;
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                        Log.v(TAG,"info output buffers changed!");
                        outputBuffers = mAudioDecoder.getOutputBuffers();

                        break;

                    default:
                        Log.v(TAG,"lyfnew writesssaudio mediaCodecInfo.offset = "+mediaCodecInfo.offset+";size = "+mediaCodecInfo.size);
                        ByteBuffer byteBuffer = outputBuffers[outputIndex];
                        final byte[] bytes = new byte[mediaCodecInfo.size];
                        byteBuffer.get(bytes);
                        byteBuffer.clear();
                        mAudioTrack.write(bytes, 0, mediaCodecInfo.size);
                        mAudioDecoder.releaseOutputBuffer(outputIndex, false);
                }
                sleepRender(mediaCodecInfo.presentationTimeUs, startMS);
                if(isEOS){
                    break;
                }
            }

        }
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
            mAudioDecoder.stop();
            mAudioDecoder.release();
            mExtractor.release();
            mAudioExtractor.release();
            mAudioTrack.release();
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

    private int getMediaTrackIndex(MediaExtractor extractor, String media_type){
        int trackIndex =-1;
        for(int i = 0; i < extractor.getTrackCount(); i++){
            String mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME);
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

    private boolean putBufferToAudioDecode(ByteBuffer[] inputBuffers){
        boolean isEos = false;
        int inputBufferIndex = mAudioDecoder.dequeueInputBuffer(-1);
        if(inputBufferIndex >= 0){
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = mAudioExtractor.readSampleData(inputBuffer, 0);
            if(sampleSize < 0){
                mAudioDecoder.queueInputBuffer(inputBufferIndex,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEos = true;
            }else{
                mAudioDecoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, mAudioExtractor.getSampleTime(),0);
                mAudioExtractor.advance();
            }
        }
        return isEos;

    }


}
