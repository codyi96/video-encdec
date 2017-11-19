package com.yie.videoencdec.encdec;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.yie.videoencdec.utils.Config;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.lang.Thread.sleep;

/**
 * Created by zou on 17-8-24.
 *
 */

public class HevcDecoder extends Decoder {
    private final static String TAG = "HevcDecoder";
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;

    private Surface mSurface;
    private int mFrameRate;

    @SuppressLint("NewApi")
    public HevcDecoder(int width, int height, int frameRate, int bitRate, Surface surface) throws IOException {
        mFrameRate = frameRate;
        mSurface = surface;

        mMediaCodec = MediaCodec.createDecoderByType(Config.TYPE_H265);

        mMediaFormat = MediaFormat.createVideoFormat(Config.TYPE_H265, width, height);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
    }

    @SuppressLint("NewApi")
    public void start() {
        mMediaCodec.configure(mMediaFormat, mSurface, null, 0);
        mMediaCodec.start();
    }

    @SuppressLint("NewApi")
    public void stop() {
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        Log.d(TAG, "Decoder is release...");
    }

    @SuppressLint("NewApi")
    public boolean offerDecoder(byte[] input, long position) {
        boolean ret = true;

        if (input == null) {
            Log.e(TAG, "offerDecoder, input is null");
            return false;
        }

        try {
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            int inputBufferIndex;
            int outputBufferIndex;
            ByteBuffer inputBuffer;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            int step = 1000000 / mFrameRate;

            inputBufferIndex = mMediaCodec.dequeueInputBuffer(step * 2);
            if (inputBufferIndex >= 0) {
                inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, position * step, 0);
            } else {
                Log.i(TAG, "failed!!! inputBufferIndex=" + inputBufferIndex + "!!!");
                ret = false;
            }

            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, step);
            while (outputBufferIndex >= 0) {
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, step);
            }
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.d(TAG, "outputBuffers changed...");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.d(TAG, "outputFormat changed...");
            } else if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.d(TAG, "timed out...");
            }
            return ret;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }
}

