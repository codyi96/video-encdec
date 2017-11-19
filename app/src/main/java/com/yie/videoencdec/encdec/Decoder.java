package com.yie.videoencdec.encdec;

/**
 * Created by zou on 17-9-11.
 *
 */

public abstract class Decoder {

    public abstract void start();
    public abstract void stop();
    public abstract boolean offerDecoder(byte[] input, long position);
}
