package com.example.xiaojin20135.bleutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.xiaojin20135.basemodule.activity.ToolBarActivity;
import com.example.xiaojin20135.basemodule.activity.login.BaseLoginActivity;
import com.example.xiaojin20135.blelib.fragment.ScanListFragment;

public class ScanListActivity extends ToolBarActivity {

    private static final String UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE = "0000fff6-0000-1000-8000-00805f9b34fb";
    private static final String UUID_NOTIFICATION = "0000fff4-0000-1000-8000-00805f9b34fb";
    private static final String UUID_CONFIRM= "0000fff3-0000-1000-8000-00805f9b34fb";
    private static final String UUID_NOTIFICATION_DES2 = "00002902-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanListFragment scanListFragment = new ScanListFragment();
        Bundle bundle = new Bundle();
        //设置蓝牙连接参数,可设可不设，如果不设，使用默认值
        bundle.putString("uuid_service",UUID_SERVICE);
        bundle.putString("uuid_write",UUID_WRITE);
        bundle.putString("uuid_notification",UUID_NOTIFICATION);
        bundle.putString("uuid_confirm",UUID_CONFIRM);
        bundle.putString("uuid_notification_des2",UUID_NOTIFICATION_DES2);

        //设置蓝牙连接成功后，跳转到的页面
        bundle.putString("className",getIntent().getStringExtra("className"));
        scanListFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment,scanListFragment).commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scan_list2;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initEvents() {

    }

    @Override
    protected void loadData() {

    }
}
