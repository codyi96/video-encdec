package com.yie.videoencdec.utils;

import android.os.StrictMode;
import android.util.Log;

import com.yie.videoencdec.encdec.AvcEncoder;
import com.yie.videoencdec.encdec.Encoder;
import com.yie.videoencdec.encdec.HevcEncoder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zou on 17-8-23.
 *
 */

public class FileEncoder extends Thread {
    private final static String TAG = "FileEncoder";

    private Spend mEncoderSpend = new Spend();
    private Encoder mEncoder;

    private String mInputFilePath = "";
    private String mOutputFilePath = "";

    private byte[] mH26XData;
    private int mSize;
    private long mFrameNum;
    private boolean mUseSame;
    private int repeat = 300;

    public FileEncoder(String inputFilePath, String outputFilePath, int width, int height,
                       int frameRate, int bitRate, String type, boolean useSame) {
        setPolicy();

        mEncoderSpend.onStart(TAG);
        mSize = width * height * 3 / 2;
        mH26XData = new byte[mSize];
        mInputFilePath = inputFilePath;
        mOutputFilePath = outputFilePath;
        mFrameNum = 0;
        mUseSame = useSame;

        try {
            if (type.equals(Config.TYPE_H264)) {
                mEncoder = new AvcEncoder(width, height, frameRate, bitRate);
            } else if (type.equals(Config.TYPE_H265)) {
                mEncoder = new HevcEncoder(width, height, frameRate, bitRate);
            }
        } catch (IOException e) {
            Log.d(TAG, "Fail to new Encoder");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        Log.i(TAG, "FileEncoder run... srcPath=" + mInputFilePath + ", tarPath=" + mOutputFilePath);
        byte[] readBuffer;
        int readSize = 0;
        int ret = 0;
        FileInputStream mFis = null;
        FileOutputStream mFos = null;

        try {
            mFis = new FileInputStream(mInputFilePath);
            mFos = new FileOutputStream(mOutputFilePath);
            mEncoder.start();

            while (!Thread.interrupted()) {
                readBuffer = new byte[mSize];
                mH26XData = new byte[mSize];
                readSize = mFis.read(readBuffer, 0, mSize);
                if (readSize > 0) {
                    if (mUseSame) {
                        for (int i = 0; i < repeat; i++) {
                            ret = mEncoder.offerEncoder(readBuffer, mH26XData);
                            if (ret > 0) {
                                mFrameNum++;
                                mFos.write(TypeChange.int2Bytes(ret));
                                mFos.write(mH26XData, 0, ret);
                                mFos.flush();
                            } else {
                                Log.i(TAG, "this frame encoder failed!!!");
                            }
                        }
                        break;
                    } else {
                        ret = mEncoder.offerEncoder(readBuffer, mH26XData);
                        if (ret > 0) {
                            mFrameNum++;
                            mFos.write(TypeChange.int2Bytes(ret));
                            mFos.write(mH26XData, 0, ret);
                            mFos.flush();
                        } else {
                            Log.i(TAG, "this frame encoder failed!!!");
                        }
                    }
                } else {
                    Log.i(TAG, "read over...");
                    break;
                }
            }
            mFis.close();
            mFos.close();
            mEncoder.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mEncoderSpend.onStop();

        printLog();
        Log.i(TAG, "FileEncoder end...");
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
        Log.i(TAG, "frameRate=" + 1000 / (mEncoderSpend.getRet() * 1.00 / mFrameNum)
                + ", frameNumber=" + mFrameNum);
    }
}
