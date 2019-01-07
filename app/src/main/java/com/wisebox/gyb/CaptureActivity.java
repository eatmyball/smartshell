package com.wisebox.gyb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class CaptureActivity extends Activity implements View.OnClickListener, QRCodeView.Delegate {
    private int REQUEST_CODE_SCAN = 299;
    private ZXingView mZxingView;
    private ImageButton imBtn_flash;
    private Button btn_back;
    private boolean isFlashOn = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_captrue);
        mZxingView = (ZXingView) findViewById(R.id.qrcode_captrue_zxingview);
        mZxingView.setDelegate(this);
        imBtn_flash = (ImageButton) findViewById(R.id.qrcode_captrue_btn_flashlight);
        imBtn_flash.setOnClickListener(this);
        btn_back = (Button) findViewById(R.id.qrcode_captrue_btn_back);
        btn_back.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mZxingView.startCamera();
        mZxingView.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        mZxingView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZxingView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.qrcode_captrue_btn_flashlight) {
            if(!isFlashOn) {
                mZxingView.openFlashlight(); // 打开闪光灯
                isFlashOn = true;
            }else {
                mZxingView.closeFlashlight(); // 关闭闪光灯
                isFlashOn = false;
            }
        }
        else if(id == R.id.qrcode_captrue_btn_back) {
            this.finish();
        }

    }

    //扫描成功
    @Override
    public void onScanQRCodeSuccess(String result) {
        vibrate();
        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putString("codedContent",result);
        intent.putExtras(data);
        setResult(REQUEST_CODE_SCAN,intent);
        this.finish();
    }

    //扫描不成功
    @Override
    public void onScanQRCodeOpenCameraError() {
        vibrate();
        new AlertDialog.Builder(this).setTitle("确认").setMessage("摄像头初始化失败，请稍后重试！")
                .setPositiveButton("确定", null).show();
    }

    //摄像头画面过暗，提示用户打开闪光灯
    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        String tipText = mZxingView.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZxingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                mZxingView.getScanBoxView().setTipText(tipText);
            }
        }
    }

    //调用系统震动
    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }
}
