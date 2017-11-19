package com.yie.videoencdec.encdec;

/**
 * Created by zou on 17-9-11.
 *
 */

public abstract class Encoder {

    public abstract void start();
    public abstract void stop();
    public abstract int offerEncoder(byte[] input, byte[] output);
}
