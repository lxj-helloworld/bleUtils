package com.example.xiaojin20135.bleutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.xiaojin20135.basemodule.activity.ToolBarActivity;
import com.example.xiaojin20135.basemodule.activity.login.BaseLoginActivity;
import com.example.xiaojin20135.blelib.fragment.ScanListFragment;

public class ScanListActivity extends ToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanListFragment scanListFragment = new ScanListFragment();
        Bundle bundle = new Bundle();
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
