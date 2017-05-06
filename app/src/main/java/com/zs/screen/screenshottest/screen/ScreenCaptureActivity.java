package com.zs.screen.screenshottest.screen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zs.screen.screenshottest.R;
import com.zs.screen.screenshottest.util.LogUtil;
import com.zs.screen.screenshottest.util.ToastUtil;
import com.zs.screen.screenshottest.view.MarkSizeView;

/**
 * 5.0版本以上可用
 */
public class ScreenCaptureActivity extends Activity {
    private String TAG = "ScreenCaptureActivity";
    private int result = 0;
    private Intent intent = null;
    private int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mMediaProjectionManager;
    private MarkSizeView markSizeView;
    private Rect markedArea;
    private MarkSizeView.GraphicPath mGraphicPath;
    private TextView captureTips;
    private Button captureAll;
    private Button markType;
    private boolean isMarkRect = true;
    private ScreenCapture screenCaptureService;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ToastUtil.show(R.string.can_not_capture_under_5_0);
            finish();
            return;
        }
//        ArcTipViewController.getInstance().showHideFloatImageView();
        initWindow();
        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        setContentView(R.layout.activity_screen_capture);
        markSizeView = (MarkSizeView) findViewById(R.id.mark_size);
        captureTips = (TextView) findViewById(R.id.capture_tips);
        captureAll = (Button) findViewById(R.id.capture_all);
        markType = (Button) findViewById(R.id.mark_type);

        markType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMarkRect = !isMarkRect;
                markSizeView.setIsMarkRect(isMarkRect);
                markType.setText(isMarkRect ? R.string.capture_type_rect : R.string.capture_type_free);
            }
        });

        markSizeView.setmOnClickListener(new MarkSizeView.onClickListener() {
            @Override
            public void onConfirm(Rect markedArea) {
                LogUtil.d(TAG, "onConfirm(Rect markedArea)");
                ScreenCaptureActivity.this.markedArea = new Rect(markedArea);
                markSizeView.reset();
                markSizeView.setUnmarkedColor(getResources().getColor(R.color.transparent));
                markSizeView.setEnabled(false);
                startIntent();
            }

            @Override
            public void onConfirm(MarkSizeView.GraphicPath path) {
                LogUtil.d(TAG, "onConfirm        (MarkSizeView.GraphicPath path)");
                mGraphicPath = path;
                markSizeView.reset();
                markSizeView.setUnmarkedColor(getResources().getColor(R.color.transparent));
                markSizeView.setEnabled(false);
                startIntent();
            }

            @Override
            public void onCancel() {
                captureTips.setVisibility(View.VISIBLE);
                captureAll.setVisibility(View.VISIBLE);
                markType.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTouch() {
                captureTips.setVisibility(View.GONE);
                captureAll.setVisibility(View.GONE);
                markType.setVisibility(View.GONE);
            }
        });

        captureAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markSizeView.setUnmarkedColor(getResources().getColor(R.color.transparent));
                captureTips.setVisibility(View.GONE);
                captureAll.setVisibility(View.GONE);
                markType.setVisibility(View.GONE);
                startIntent();
            }
        });

    }

    private void initWindow() {
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.trans));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startIntent() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (screenCaptureService != null)
            screenCaptureService.onDestroy();
        super.onDestroy();
    }

    private void startScreenCapture(Intent intent, int resultCode) {
        screenCaptureService = new ScreenCapture(this, intent, resultCode, markedArea, mGraphicPath);
        try {
            screenCaptureService.toCapture();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d(TAG, "进入了");
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            } else if (data != null && resultCode != 0) {
                LogUtil.i(TAG, "user agree the application to capture screen");
                result = resultCode;
                intent = data;
                startScreenCapture(data, resultCode);
                LogUtil.i(TAG, "start service ScreenCaptureService");
            }
        }
    }


}
