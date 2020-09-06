package com.example.luoyangfan.myapplication;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private Context mContext;

    private final static String DRAW_VERTEX_SHADER = "" +
            "attribute vec4 av_Position;\n" +
            "attribute vec2 af_Position;\n" +
            "varying vec2 v_texPosition;\n" +
            "void main(){\n" +
            "    gl_Position = av_Position;\n" +
            "    v_texPosition = af_Position;\n" +
            "}\n";

    private final static String DRAW_FRAG_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision highp float;\n" +
            "varying highp vec2 v_texPosition;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main(){\n" +
            "    vec2 uv = v_texPosition;\n" +
            "    if(uv.y <= 0.5){\n" +
            "         uv.y = uv.y * 2.0; \n" +
            "    }else{\n"+
            "         uv.y = (uv.y - 0.5) * 2.0; \n" +
            "    }\n" +
            "    vec4 texture = texture2D(sTexture, uv);\n" +
            "    texture.a = 0.7; \n" +
            "    gl_FragColor = texture;\n" +
            "}\n";

    private final float[] VertexData = {
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
    };

    private final float[] TextureData = {
            //0f, 1f,
            //1f, 1f,
            //0f, 0f,
            //1f, 0f
          0.0f,0.0f,


           0.0f,1.0f,

            1.0f,0.0f,

            1.0f,1.0f
    };

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    private int mProgram;
    private int mVPositionHandle;
    private int mTPositionHandle;
    private int mOESTextureHandle;
    private int mTextureID;

    private SurfaceTexture mSurTexture;
    private Surface mSurface;

    public interface OnSurfaceCreateListener{
        void onSurfaceCreate(Surface surface);
    }

    public void setmOnSurfaceCreateListener(OnSurfaceCreateListener mOnSurfaceCreateListener) {
        this.mOnSurfaceCreateListener = mOnSurfaceCreateListener;
    }

    private OnSurfaceCreateListener mOnSurfaceCreateListener;

    public interface OnRenderListener{
        void onRender();
    }

    public void setmOnRenderListener(OnRenderListener mOnRenderListener) {
        this.mOnRenderListener = mOnRenderListener;
    }

    private OnRenderListener mOnRenderListener;


    public VideoRender(Context context){
        mContext = context;
        mVertexBuffer = ByteBuffer.allocateDirect(VertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(VertexData);
        mVertexBuffer.position(0);
        mTextureBuffer = ByteBuffer.allocateDirect(TextureData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(TextureData);
        mTextureBuffer.position(0);

    }


    private void initGLRender(){
        mProgram = ShaderUtil.createProgram(DRAW_VERTEX_SHADER, DRAW_FRAG_SHADER);
        mVPositionHandle = GLES20.glGetAttribLocation(mProgram, "av_Position");
        mTPositionHandle = GLES20.glGetAttribLocation(mProgram, "af_Position");
        mOESTextureHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");

        int[] textureids = new int[1];
        GLES20.glGenTextures(1,textureids, 0);
        mTextureID = textureids[0];

        mSurTexture = new SurfaceTexture(mTextureID);
        mSurface = new Surface(mSurTexture);
        mSurTexture.setOnFrameAvailableListener(this);
        if(mOnSurfaceCreateListener != null){
            mOnSurfaceCreateListener.onSurfaceCreate(mSurface);
        }

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if(mOnRenderListener != null){
            mOnRenderListener.onRender();
        }

    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        initGLRender();

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0,0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        renderMediacodec();
    }

    private void renderMediacodec(){
        mSurTexture.updateTexImage();
        GLES20.glUseProgram(mProgram);

        GLES20.glEnableVertexAttribArray(mVPositionHandle);
        GLES20.glVertexAttribPointer(mVPositionHandle, 2, GLES20.GL_FLOAT, false, 2*4, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTPositionHandle);
        GLES20.glVertexAttribPointer(mTPositionHandle,2, GLES20.GL_FLOAT, false, 2*4, mTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        GLES20.glUniform1i(mOESTextureHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0, 4);
    }
}
