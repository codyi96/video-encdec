package com.yie.videoencdec.utils;

/**
 * Created by zou on 17-9-8.
 *
 */

public final class Config {

    //code type
    public final static String TYPE_H264 = "video/avc";
    public final static String TYPE_H265 = "video/hevc";

    //operate type
    public final static String TYPE_ENCODE = "encode";
    public final static String TYPE_DECODE = "decode";
    public final static String TYPE_ENCDEC = "encdec";

    //file path
    public final static String FP_DIR = "/data/starnet/yuv/";

    //file name
    public final static String FN_YUV_EN1080 = "en1080.yuv";
    public final static String FN_YUV_EN4k = "en4k.yuv";
    public final static String FN_YUV_EN720 = "en720.yuv";

    public final static String FN_H264_EN1080 = "en1080.h264";
    public final static String FN_H264_EN4k = "en4k.h264";
    public final static String FN_H264_EN720 = "en720.h264";
    public final static String FN_H265_EN1080 = "en1080.h265";
    public final static String FN_H265_EN4k = "en4k.h265";
    public final static String FN_H265_EN720 = "en720.h265";

    public final static String FN_H264_DE1080 = "de1080.h264";
    public final static String FN_H264_DE4k = "de4k.h264";
    public final static String FN_H264_DE720 = "de720.h264";

    public final static String FN_H265_DE1080 = "de1080.h265";
    public final static String FN_H265_DE4k = "de4k.h265";
    public final static String FN_H265_DE720 = "de720.h265";

    public final static int START_CODE_4B_H264 = 0x00000001;
    public final static int START_CODE_3B_H264 = 0x000001;
}
