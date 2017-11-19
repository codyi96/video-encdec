package com.yie.videoencdec.utils;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zou on 17-8-23.
 *
 */

public class Spend {
    private String mTag = "Spend";
    private long mStartTime = 0;
    private long mEndTime = 0;
    private long mRet = 0;
    private long mScale = 1000000;
    private List<Long> time = new LinkedList<>();

    public void onStart(String tag) {
        if (tag != null) {
            mTag = tag;
        }
        mStartTime = System.nanoTime();
        Log.i(mTag, "Spend onStart...");
    }

    public void onStop() {
        if (mStartTime == 0) {
            Log.i(mTag, "Spend onStop: Please onStart before");
        } else {
            mEndTime = System.nanoTime();
            mRet = (mEndTime - mStartTime) / mScale;
            Log.i(mTag, "it took " + mRet + " ms");
            time.add(mRet);
        }
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public long getAveTime() {
        if (time == null || time.size() == 0)
            return 0;
        long sum = 0;
        long ave = 0;
        for (long t : time) {
            sum += t;
        }
        ave = sum / time.size();
        Log.d(mTag, "time : " + time.toString());
        return ave;
    }

    public long getRet() {
        return mRet;
    }
}
