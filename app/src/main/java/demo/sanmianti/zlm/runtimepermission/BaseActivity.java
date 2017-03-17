package demo.sanmianti.zlm.runtimepermission;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * description: 公共activity类
 * author: zlm
 * date: 2017/3/17 12:37
 */
public class BaseActivity extends AppCompatActivity {

    private RequestPermissionCallBack mRequestPermissionCallBack;
    private final int mRequestCode = 1024;
    private String[] mPermissionsName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 权限请求结果回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case mRequestCode: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mRequestPermissionCallBack.granted();

                } else {
                    //在用户已经拒绝授权的情况下，如果shouldShowRequestPermissionRationale返回false则
                    // 可以推断出用户选择了“不在提示”选项，在这种情况下需要引导用户至设置页手动授权
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        new AlertDialog.Builder(this)
                                .setMessage("用户选择了不在提示按钮，或者系统默认不在提示（如MIUI）。提醒用户：获取"
                                        + mPermissionsName[0] + "失败，将导致部分功能无法正常使用，需要到设置页面手动授权")
                                .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mRequestPermissionCallBack.denied();
                                    }
                                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        mRequestPermissionCallBack.denied();
                                    }
                                }).show();

                    }else {
                        mRequestPermissionCallBack.denied();
                    }
                }
            }
        }
    }

    /**
     * 发起权限请求
     *
     * @param context
     * @param permissions
     * @param callback
     */
    public void requestPermissions(final Context context, final String[] permissions,
                                   final String[] permissionNames, RequestPermissionCallBack callback) {
        this.mRequestPermissionCallBack = callback;
        this.mPermissionsName = permissionNames;

        if (ContextCompat.checkSelfPermission(context, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            mRequestPermissionCallBack.granted();
        } else {
            //用户曾经拒绝过我们的权限请求，再次请求时候弹出对话框向用户解释一下原因
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permissions[0])) {
                new AlertDialog.Builder(context)
                        .setMessage("您好，需要" + permissionNames[0] + "，否则将影响部分功能的正常使用。")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(((Activity) context), permissions, mRequestCode);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(((Activity) context), permissions, mRequestCode);
            }
        }

    }

    /**
     * 权限请求结果回调接口
     */
     interface RequestPermissionCallBack {
        /**
         * 同意授权
         */
        public  void granted();

        /**
         * 取消授权
         */
        public  void denied();
    }
}
