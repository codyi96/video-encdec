package com.yie.videoencdec.utils;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by zou on 17-8-23.
 *
 */

public class AvcEncoder {
    private final static String TAG = "AvcEncoder";
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;

    private String mCodecType = "video/avc";
    private int mStartCode4byte = 0x00000001;
    private int mStartCode3byte = 0x000001;
    //PPS SPS信息
    private byte[] mInfo = null;

    @SuppressLint("NewApi")
    AvcEncoder(int width, int height, int frameRate, int bitRate) throws IOException {
        mMediaCodec = MediaCodec.createEncoderByType(mCodecType);

        mMediaFormat = MediaFormat.createVideoFormat(mCodecType, width, height);
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
     * @param output H264
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
            ByteBuffer mSPSPPSBuffer;

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
                outputBuffer.get(outData);
                if (mInfo != null) {
                    System.arraycopy(outData, 0, output, position, outData.length);
                    position += outData.length;
                } else {
                    mSPSPPSBuffer = ByteBuffer.wrap(outData);
                    if (mSPSPPSBuffer.getInt() == mStartCode4byte
                            || (mSPSPPSBuffer.getInt() >> 8) == mStartCode3byte) {
                        mInfo = new byte[outData.length];
                        System.arraycopy(outData, 0, mInfo, 0, outData.length);
                    } else {
                        return -1;
                    }
                }

                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }

            //编码器生成关键帧只有00 00 00 01 65， 没有PPS SPS
            if(output[3] == 0x01 && output[4] == 0x65) {
                //借用input做temp
                System.arraycopy(output, 0, input, 0, position);
                System.arraycopy(mInfo, 0, output, 0, mInfo.length);
                System.arraycopy(input, 0,  output, mInfo.length, position);
                position += mInfo.length;
                Log.d(TAG, "is key frame");
            } else {
                Log.d(TAG, "isn't key frame");
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        return position;
    }
}
