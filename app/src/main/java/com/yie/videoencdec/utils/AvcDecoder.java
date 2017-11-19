package com.yie.videoencdec.utils;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zou on 17-8-24.
 *
 */

public class AvcDecoder {
    private final static String TAG = "AvcDecoder";
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;

    private String mCodecType = "video/avc";
    private Surface mSurface;
    private int mFrameRate;

    @SuppressLint("NewApi")
    AvcDecoder(int width, int height, int frameRate, int bitRate, Surface surface) throws IOException {
        mFrameRate = frameRate;
        mSurface = surface;

        mMediaCodec = MediaCodec.createDecoderByType(mCodecType);

        mMediaFormat = MediaFormat.createVideoFormat(mCodecType, width, height);
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
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
            int inputBufferIndex;
            int outputBufferIndex;
            ByteBuffer inputBuffer;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            int step = 1000000 / mFrameRate;

            inputBufferIndex = mMediaCodec.dequeueInputBuffer(step);
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
//                    outputBuffers = mMediaCodec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.d(TAG, "outputFormat changed...");
//                    MediaFormat format = mMediaCodec.getOutputFormat();
//                    mMediaCodec.stop();
//                    mMediaCodec.configure(format, mSurface, null, 0);
//                    mMediaCodec.start();
            }
            return ret;

        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }
}

