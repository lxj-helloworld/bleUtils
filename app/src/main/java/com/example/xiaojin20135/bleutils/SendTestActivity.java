package com.example.xiaojin20135.bleutils;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.xiaojin20135.basemodule.activity.BaseActivity;
import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.FrameReceivedLis;
import com.example.xiaojin20135.blelib.helps.BleConstant;
import com.example.xiaojin20135.blelib.helps.DatasBuffer;
import com.example.xiaojin20135.blelib.helps.MethodsUtil;

public class SendTestActivity extends BaseActivity {
    private static final String TAG = "SendTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TestFragment testFragment = new TestFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_fragment,testFragment).commit();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_send_test;
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
