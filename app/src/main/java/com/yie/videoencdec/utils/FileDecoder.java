package com.yie.videoencdec.utils;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;
import android.view.Surface;


import com.yie.videoencdec.encdec.AvcDecoder;
import com.yie.videoencdec.encdec.Decoder;
import com.yie.videoencdec.encdec.HevcDecoder;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by zou on 17-8-23.
 *
 */

public class FileDecoder extends Thread {
    private final static String TAG = "FileDecoder";

    private Spend mDecoderSpend = new Spend();
    private Decoder mDecoder;

    private String mInputFilePath = "";
    private long mFrameNum;
    private boolean mUseSame;
    private int repeat = 300;

    public FileDecoder(String inputFilePath, Surface surface, int frameRate, int bitRate,
                       int width, int height, String type, boolean useSame) {
        setPolicy();

        mDecoderSpend.onStart(TAG);
        mInputFilePath = inputFilePath;
        mFrameNum = 0;
        mUseSame = useSame;

        try {
            if (type.equals(Config.TYPE_H264)) {
                mDecoder = new AvcDecoder(width, height, frameRate, bitRate, surface);
            } else if (type.equals(Config.TYPE_H265)) {
                mDecoder = new HevcDecoder(width, height, frameRate, bitRate, surface);
            }
        } catch (IOException e) {
            Log.d(TAG, "Fail to new Decoder");
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void run() {

        Log.i(TAG, "FileDecoder run... path=" + mInputFilePath);
        FileInputStream mFis = null;
        byte[] h26XHead = null;
        byte[] h26XData = null;
        int readRet = -1;
        int dataSize = -1;

        try {
            mFis = new FileInputStream(mInputFilePath);
            mDecoder.start();
            while (!Thread.interrupted()) {
                h26XHead = new byte[4];
                readRet = mFis.read(h26XHead);
                if (readRet < 0) {
                    Log.i(TAG, "Can't read h26XHead, position=" + readRet);
                    break;
                }
                dataSize = TypeChange.bytes2Int(h26XHead, 0);
                h26XData = new byte[dataSize];
                readRet = mFis.read(h26XData, 0, dataSize);
                if (readRet < 0) {
                    Log.i(TAG, "Can't read h26XData, position=" + readRet);
                    break;
                }
                if (mUseSame) {
                    for (int i = 0; i < repeat; i++) {
                        if (mDecoder.offerDecoder(h26XData, mFrameNum)) {
                            mFrameNum++;
                        }
                    }
                    break;
                } else {
                    if (mDecoder.offerDecoder(h26XData, mFrameNum)) {
                        mFrameNum++;
                    }
                }
            }
            mFis.close();
            mDecoder.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDecoderSpend.onStop();

        printLog();
        Log.i(TAG, "Decoder end...");
    }

    private void setPolicy() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    private void printLog() {
        Log.i(TAG, "frameRate=" + 1000 / (mDecoderSpend.getRet() * 1.00 / mFrameNum)
                + ", frameNumber=" + mFrameNum);
    }

}
