package com.yie.videoencdec;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yie.videoencdec.utils.CPUInfo;
import com.yie.videoencdec.utils.Config;
import com.yie.videoencdec.utils.FileEncoder;
import com.yie.videoencdec.utils.SurHolderCallback;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends Activity {

    //数据变量
    private FileEncoder mEncoder;
    private List<SurHolderCallback> mSurHolderCallbackList;
    private String mType = Config.TYPE_H264;
    private CPUInfo mCpuInfo;

    //控件变量
    private EditText mEditEncoder;
    private EditText mEditEncoderFrame;
    private EditText mEditEncoderBit;
    private EditText mEditDecoder;
    private EditText mEditDecoderFrame;
    private EditText mEditDecoderBit;
    private Button mBtn720;
    private Button mBtn1080;
    private Button mBtn4k;
    private SurfaceView mSurfaceView1;
    private SurfaceView mSurfaceView2;
    private SurfaceView mSurfaceView3;
    private SurfaceView mSurfaceView4;
    private CheckBox mIsSameSourceBox;
    private CheckBox mIsBigViewBox;
    private CheckBox mIsEncBox;
    private CheckBox mIsDecBox;
    private CheckBox mUseSame;
    private RadioGroup mCodeTypeGroup;
    private RadioButton mCurrentRadioButton;
    private TextView mTextCpuInfo;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isEnc = mIsEncBox.isChecked();
            boolean isDec = mIsDecBox.isChecked();
            boolean isSameSource = mIsSameSourceBox.isChecked();
            boolean isBigView = mIsBigViewBox.isChecked();
            boolean isUseSame = mUseSame.isChecked();


            int enNum = Integer.valueOf(mEditEncoder.getText().toString());
            int enFrame = Integer.valueOf(mEditEncoderFrame.getText().toString());
            int enBit = Integer.valueOf(mEditEncoderBit.getText().toString()) * 1000;

            int deNum = Integer.valueOf(mEditDecoder.getText().toString());
            int deFrame = Integer.valueOf(mEditDecoderFrame.getText().toString());
            int deBit = Integer.valueOf(mEditDecoderBit.getText().toString()) * 1000;

            switch (view.getId()) {

                case R.id.btn_720:
                    if (isEnc) {
                        enc720(enNum, enFrame, enBit, isUseSame);
                    }
                    if (isDec) {
                        dec720(deNum, deFrame, deBit, isSameSource, isBigView, isUseSame);
                    }
                    break;

                case R.id.btn_1080:
                    if (isEnc) {
                        enc1080(enNum, enFrame, enBit, isUseSame);
                    }
                    if (isDec) {
                        dec1080(deNum, deFrame, deBit, isSameSource, isBigView, isUseSame);
                    }
                    break;

                case R.id.btn_4k:
                    if (isEnc) {
                        enc4k(enNum, enFrame, enBit, isUseSame);
                    }
                    if (isDec) {
                        dec4k(deNum, deFrame, deBit, isSameSource, isBigView, isUseSame);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
            mCurrentRadioButton = (RadioButton) findViewById(checkedId);
            String code = mCurrentRadioButton.getText().toString();
            if (code.equals(getResources().getString(R.string.h264))) {
                mType = Config.TYPE_H264;
            } else if (code.equals(getResources().getString(R.string.h265))) {
                mType = Config.TYPE_H265;
            }
        }
    };

    private Handler mCpuHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CPUInfo.ID) {
                mTextCpuInfo.setText(msg.obj.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();

    }

    private void enc720(int enNum, int enFrame, int enBit, boolean useSame) {

        String savePath = "";
        if (mType.equals(Config.TYPE_H264)) {
            savePath = Config.FP_DIR + Config.FN_H264_EN720;
        } else if (mType.equals(Config.TYPE_H265)) {
            savePath = Config.FP_DIR + Config.FN_H265_EN720;
        }

        for (int i = 0; i < enNum; i++) {
            mEncoder = new FileEncoder(Config.FP_DIR + Config.FN_YUV_EN720,
                    savePath + i, 1280, 720, enFrame, enBit, mType, useSame);
            mEncoder.start();
        }
    }

    private void dec720(int deNum, int deFrame, int deBit, boolean isSameSource, boolean isBigView, boolean useSame) {

        if (isBigView) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(1280,
                    720);
            mSurfaceView1.setLayoutParams(lp);
        } else {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mSurfaceView1.setLayoutParams(lp);
        }

        String fromPath = "";
        if (isSameSource) {
            if (mType.equals(Config.TYPE_H264)) {
                fromPath = Config.FP_DIR + Config.FN_H264_EN720;
            } else if (mType.equals(Config.TYPE_H265)) {
                fromPath = Config.FP_DIR + Config.FN_H265_EN720;
            }
        } else {
            if (mType.equals(Config.TYPE_H264)) {
                fromPath = Config.FP_DIR + Config.FN_H264_DE720;
            } else if (mType.equals(Config.TYPE_H265)) {
                fromPath = Config.FP_DIR + Config.FN_H265_DE720;
            }
        }
        for (int i = 0; i < deNum; i++) {
            mSurHolderCallbackList.get(i).onReStart(fromPath + i,
                    deFrame, deBit, 1280, 720, mType, useSame);
        }
    }

    private void enc1080(int enNum, int enFrame, int enBit, boolean useSame) {

        String savePath = "";
        if (mType.equals(Config.TYPE_H264)) {
            savePath = Config.FP_DIR + Config.FN_H264_EN1080;
        } else if (mType.equals(Config.TYPE_H265)) {
            savePath = Config.FP_DIR + Config.FN_H265_EN1080;
        }

        for (int i = 0; i < enNum; i++) {
            mEncoder = new FileEncoder(Config.FP_DIR + Config.FN_YUV_EN1080,
                    savePath + i, 1920, 1080, enFrame, enBit, mType, useSame);
            mEncoder.start();
        }
    }

    private void dec1080(int deNum, int deFrame, int deBit, boolean isSameSource, boolean isBigView, boolean useSame) {

        if (isBigView) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(1920,
                    1080);
            mSurfaceView1.setLayoutParams(lp);
        } else {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mSurfaceView1.setLayoutParams(lp);
        }

        String fromPath = "";
        if (isSameSource) {
            if (mType.equals(Config.TYPE_H264)) {
                fromPath = Config.FP_DIR + Config.FN_H264_EN1080;
            } else if (mType.equals(Config.TYPE_H265)) {
                fromPath = Config.FP_DIR + Config.FN_H265_EN1080;
            }
        } else {
            if (mType.equals(Config.TYPE_H264)) {
                fromPath = Config.FP_DIR + Config.FN_H264_DE1080;
            } else if (mType.equals(Config.TYPE_H265)) {
                fromPath = Config.FP_DIR + Config.FN_H265_DE1080;
            }
        }
        for (int i = 0; i < deNum; i++) {
            mSurHolderCallbackList.get(i).onReStart(fromPath + i,
                    deFrame, deBit, 1920, 1080, mType, useSame);
        }
    }

    private void enc4k(int enNum, int enFrame, int enBit, boolean useSame) {

        String savePath = "";
        if (mType.equals(Config.TYPE_H264)) {
            savePath = Config.FP_DIR + Config.FN_H264_EN4k;
        } else if (mType.equals(Config.TYPE_H265)) {
            savePath = Config.FP_DIR + Config.FN_H265_EN4k;
        }

        for (int i = 0; i < enNum; i++) {
            mEncoder = new FileEncoder(Config.FP_DIR + Config.FN_YUV_EN4k,
                    savePath + i, 3840, 2160, enFrame, enBit, mType, useSame);
            mEncoder.start();
        }

    }

    private void dec4k(int deNum, int deFrame, int deBit, boolean isSameSource, boolean isBigView, boolean useSame) {

        if (isBigView) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(3840,
                    2160);
            mSurfaceView1.setLayoutParams(lp);
        } else {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mSurfaceView1.setLayoutParams(lp);
        }

        String fromPath = "";
        if (isSameSource) {
            if (mType.equals(Config.TYPE_H264)) {
                fromPath = Config.FP_DIR + Config.FN_H264_EN4k;
            } else if (mType.equals(Config.TYPE_H265)) {
                fromPath = Config.FP_DIR + Config.FN_H265_EN4k;
            }
        } else {
            if (mType.equals(Config.TYPE_H264)) {
                fromPath = Config.FP_DIR + Config.FN_H264_DE4k;
            } else if (mType.equals(Config.TYPE_H265)) {
                fromPath = Config.FP_DIR + Config.FN_H265_DE4k;
            }
        }

        for (int i = 0; i < deNum; i++) {
            mSurHolderCallbackList.get(i).onReStart(fromPath + i,
                    deFrame, deBit, 3840, 2160, mType, useSame);
        }
    }

    private void initView() {
        mSurfaceView1 = (SurfaceView) findViewById(R.id.video1);
        mSurfaceView2 = (SurfaceView) findViewById(R.id.video2);
        mSurfaceView3 = (SurfaceView) findViewById(R.id.video3);
        mSurfaceView4 = (SurfaceView) findViewById(R.id.video4);

        mEditEncoder = (EditText) findViewById(R.id.edit_encoder);
        mEditEncoderFrame = (EditText) findViewById(R.id.edit_encoder_frame);
        mEditEncoderBit = (EditText) findViewById(R.id.edit_encoder_bit);

        mEditDecoder = (EditText) findViewById(R.id.edit_decoder);
        mEditDecoderFrame = (EditText) findViewById(R.id.edit_decoder_frame);
        mEditDecoderBit = (EditText) findViewById(R.id.edit_decoder_bit);

        mBtn720 = (Button) findViewById(R.id.btn_720);
        mBtn1080 = (Button) findViewById(R.id.btn_1080);
        mBtn4k = (Button) findViewById(R.id.btn_4k);
        mCodeTypeGroup = (RadioGroup) findViewById(R.id.code_type);
        mUseSame = (CheckBox) findViewById(R.id.use_same_box);

        mTextCpuInfo = (TextView) findViewById(R.id.cpu_info);
    }

    private void initData() {
        mBtn720.setOnClickListener(mOnClickListener);
        mBtn1080.setOnClickListener(mOnClickListener);
        mBtn4k.setOnClickListener(mOnClickListener);
        mCodeTypeGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mIsSameSourceBox = (CheckBox) findViewById(R.id.is_same_source);
        mIsBigViewBox = (CheckBox) findViewById(R.id.is_big_view);
        mIsEncBox = (CheckBox) findViewById(R.id.is_enc);
        mIsDecBox = (CheckBox) findViewById(R.id.is_dec);

        SurHolderCallback mSurHolderCallback1 = new SurHolderCallback(mSurfaceView1);
        SurHolderCallback mSurHolderCallback2 = new SurHolderCallback(mSurfaceView2);
        SurHolderCallback mSurHolderCallback3 = new SurHolderCallback(mSurfaceView3);
        SurHolderCallback mSurHolderCallback4 = new SurHolderCallback(mSurfaceView4);

        mSurHolderCallbackList = new LinkedList<>();
        mSurHolderCallbackList.add(mSurHolderCallback1);
        mSurHolderCallbackList.add(mSurHolderCallback2);
        mSurHolderCallbackList.add(mSurHolderCallback3);
        mSurHolderCallbackList.add(mSurHolderCallback4);

        mCpuInfo = new CPUInfo(mCpuHandler);
        mCpuInfo.start();
    }
}
