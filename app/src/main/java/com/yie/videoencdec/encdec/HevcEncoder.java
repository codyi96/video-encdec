package com.yie.videoencdec.encdec;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.yie.videoencdec.utils.Config;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zou on 17-8-23.
 *
 */

public class HevcEncoder extends Encoder {
    private final static String TAG = "HevcEncoder";
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;

    @SuppressLint("NewApi")
    public HevcEncoder(int width, int height, int frameRate, int bitRate) throws IOException {
        mMediaCodec = MediaCodec.createEncoderByType(Config.TYPE_H265);

        mMediaFormat = MediaFormat.createVideoFormat(Config.TYPE_H265, width, height);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
    }

    @SuppressLint("NewApi")
    public void start() {
        if (mMediaCodec == null || mMediaFormat == null) {
            Log.e(TAG, "Can't find MediaCodec/MediaFormat");
            return;
        }
        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
    }


    @SuppressLint("NewApi")
    public void stop() {
        if (mMediaCodec == null)
            return;
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        Log.d(TAG, "Encoder is release...");
    }

    /**
     *
     * @param input YUV420
     * @param output H265
     * @return 数据大小
     */
    @SuppressLint("NewApi")
    public int offerEncoder(byte[] input, byte[] output) {
        if (input == null || output == null) {
            Log.e(TAG, "offerEncoder, input/output is null");
            return -1;
        }

        int position = 0;
        try {
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
            int inputBufferIndex;
            int outputBufferIndex;
            ByteBuffer inputBuffer;
            ByteBuffer outputBuffer;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            byte[] outData = null;

            inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);
                mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
            }

            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            while (outputBufferIndex >= 0) {
                outputBuffer = outputBuffers[outputBufferIndex];
                outData = new byte[bufferInfo.size];
                Log.i(TAG, "bufferInfo size = " + bufferInfo.size);
                outputBuffer.get(outData);
                System.arraycopy(outData, 0, output, position, outData.length);
                position += outData.length;

                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return position;
    }
}