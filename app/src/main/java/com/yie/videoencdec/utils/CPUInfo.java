package com.yie.videoencdec.utils;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by zou on 17-9-11.
 *
 */

public class CPUInfo extends Thread {
    private final static String TAG = "CPUInfo";
    public final static int ID = 0x6667;
    private Handler mHandler;

    public CPUInfo(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            int user, nice, sys, idle, iowait, irq, softirq;
            int idle1, idle2, total1, total2;
            double ret;

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader("/proc/stat"));
                String str = null;
                str = reader.readLine();
                String[] listStrings = str.split(" ");
                user = Integer.parseInt(listStrings[2]);
                nice = Integer.parseInt(listStrings[3]);
                sys = Integer.parseInt(listStrings[4]);
                idle = Integer.parseInt(listStrings[5]);
                iowait = Integer.parseInt(listStrings[6]);
                irq = Integer.parseInt(listStrings[7]);
                softirq = Integer.parseInt(listStrings[8]);

                total1 = user + nice + sys + idle + iowait + irq + softirq;
                idle1 = idle;
                reader.close();

                Thread.sleep(500);


                reader = new BufferedReader(new FileReader("/proc/stat"));
                str = reader.readLine();
                listStrings = str.split(" ");
                user = Integer.parseInt(listStrings[2]);
                nice = Integer.parseInt(listStrings[3]);
                sys = Integer.parseInt(listStrings[4]);
                idle = Integer.parseInt(listStrings[5]);
                iowait = Integer.parseInt(listStrings[6]);
                irq = Integer.parseInt(listStrings[7]);
                softirq = Integer.parseInt(listStrings[8]);

                total2 = user + nice + sys + idle + iowait + irq + softirq;
                idle2 = idle;
                reader.close();

                ret = (1.00 - (idle2 - idle1) * 1.00 / (total2 - total1));
//                Log.d(TAG, "current cpu useRate:" + ret);

                Message msg = new Message();
                msg.what = ID;
                msg.obj = ret;
                mHandler.sendMessage(msg);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
