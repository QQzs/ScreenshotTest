package com.zs.screen.screenshottest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zs.screen.screenshottest.screen.ScreenCaptureActivity;
import com.zs.screen.screenshottest.util.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private View mView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mView = findViewById(R.id.v_main);
    }

    public void getScreen(View view){
        if (Build.VERSION.SDK_INT >= 23) {
            int checkWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkWritePermission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 125);
                return;
            } else {
                makeScreen();
            }
        } else if (Build.VERSION.SDK_INT >= 21) {
            makeScreen();
        } else {
            // 5.0 以下可用
            getScreen2();
        }

    }

    /**
     *
     *
     */
    public void getScreen2(){

        int[] location = new int[2];
        mView.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        Log.d("My_Log","x = " + x + "  y = " + y);

        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        //获取状态栏高度
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Log.d("My_Log","statusBarHeight = " + statusBarHeight);
        System.out.println(statusBarHeight);//获取屏幕长和高

        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindowManager().getDefaultDisplay().getHeight();//去掉标题栏
//        Bitmap bm = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        Bitmap bm = Bitmap.createBitmap(b1 , x , y, mView.getWidth(), mView.getHeight());
        Log.d("My_Log","width = " + width + " height = " + height);
        view.destroyDrawingCache();
        saveBitmapTofile(bm,System.currentTimeMillis() + ".jpg");

    }
    /**
     * 保存图片到指定文件夹
     *
     * @param bmp
     * @param filename
     * @return
     */
    private boolean saveBitmapTofile(Bitmap bmp, String filename) {
        if (bmp == null || filename == null)
            return false;
        File f = new File("/sdcard/AAB/");
        if (!f.exists()){
            f.mkdirs();
        }
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream("/sdcard/AAB/" + filename);
            Toast.makeText(this, "YES", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }

    /**
     * 创建文件夹，跳转去截图
     */
    private void makeScreen(){
        File f = new File("/sdcard/AAB/");
        if (!f.exists()){
            f.mkdirs();
        }

        Intent intent = new Intent(this, ScreenCaptureActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 125:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    makeScreen();
                }else{
                    ToastUtil.show("权限打开失败");
                }
                break;
        }
    }
}
