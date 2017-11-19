package com.yie.videoencdec.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by zou on 17-8-23.
 *
 */

public class TypeChange {
    private final static String TAG = "TypeChange";


    /**
     * byte转int， 低位在前 高位在后
     *
     * @param src    byte
     * @param offset 转换起点
     * @return 转换值
     */
    public static int bytes2Int(byte[] src, int offset) {
        if (src == null || offset < 0)
            throw new IllegalArgumentException("bytes2int failed");
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    /**
     * int转byte， 低位在前 高位在后
     *
     * @param src int
     * @return 转换值
     */
    public static byte[] int2Bytes(int src) {
        byte[] value = new byte[4];
        value[3] = (byte) ((src >> 24) & 0xFF);
        value[2] = (byte) ((src >> 16) & 0xFF);
        value[1] = (byte) ((src >> 8) & 0xFF);
        value[0] = (byte) (src & 0xFF);
        return value;
    }

    /**
     * 亮度Y不变 交换色度UV
     *
     * @param yv12   yv12数据 in
     * @param i420   i420数据 out
     * @param width  宽度
     * @param height 高度
     */
    public static void swapYV12ToI420(byte[] yv12, byte[] i420, int width, int height) {
        if (yv12 == null)
            return;
        int ySize = width * height;
        System.arraycopy(yv12, 0, i420, 0, ySize);
        System.arraycopy(yv12, ySize + ySize / 4, i420, ySize, ySize / 4);
        System.arraycopy(yv12, ySize, i420, ySize + ySize / 4, ySize / 4);
    }

    /**
     * 亮度Y不变 色度UV改为交叉存放
     *
     * @param yv12     yv12数据 in
     * @param yuv420sp i420数据 out
     * @param width    宽度
     * @param height   高度
     */
    public static void swapYV12ToYUV420SemiPlanar(byte[] yv12, byte[] yuv420sp, int width, int height) {
        if (yv12 == null)
            return;
        int ySize = width * height;
        int yv12UStart = ySize;
        int yv12VStart = ySize + ySize / 4;
        System.arraycopy(yv12, 0, yuv420sp, 0, ySize);
        for (int i = 0; i < ySize / 4; i++) {
            yuv420sp[i * 2] = yv12[yv12UStart + i];
            yuv420sp[i * 2 + 1] = yv12[yv12VStart + i];
        }
    }

    public static Bitmap bytes2Bitmap(byte[] bytes) {
        if (bytes != null) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        return null;
    }

    public static Bitmap yuv2Bitmap(byte[] nv21, int width, int height) {
        Bitmap bitmap = null;
        try {
            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 80, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void yuv420SemiPlanar2RGB(byte[] yuv420sp, int[] rgb, int width, int height) {
        int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width;
            int u = 0;
            int v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

}
