package com.yie.videoencdec.utils;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by zou on 17-8-24.
 *
 */

public class SurHolderCallback implements SurfaceHolder.Callback {
    private final static String TAG = "SurHolderCallback";
    private FileDecoder mDecoder;
    private SurfaceHolder mSurfaceHolder;

    private SurfaceView mSurfaceView;
    private String mPath;
    private int mFrameRate;
    private int mBitRate;
    private int mWidth;
    private int mHeight;
    private String mType;

    public SurHolderCallback(SurfaceView surfaceView) {
        System.out.println("SurHolderCallback Created");

        mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    public void onReStart(String path, int frameRate, int bitRate, int width, int height, String type, boolean useSame) {
        mPath = path;
        mFrameRate = frameRate;
        mBitRate = bitRate;
        mWidth = width;
        mHeight = height;
        mType = type;

        if (mDecoder != null && mDecoder.isAlive()) {
            Log.i(TAG, "is Alive!!!");
            mDecoder.interrupt();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mDecoder = new FileDecoder(mPath, mSurfaceHolder.getSurface(), mFrameRate, mBitRate,
                mWidth, mHeight, mType, useSame);
        mDecoder.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        System.out.println("SurfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        System.out.println("SurfaceDestroyed");
    }
}
