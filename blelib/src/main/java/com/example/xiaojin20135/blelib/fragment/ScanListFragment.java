package com.example.xiaojin20135.blelib.fragment;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.xiaojin20135.basemodule.fragment.base.BaseFragment;
import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.R;
import com.example.xiaojin20135.blelib.adapter.BleDeviceAdapter;
import com.example.xiaojin20135.blelib.bean.MyBluetoothDevice;
import com.example.xiaojin20135.blelib.helps.BleConstant;

import java.util.ArrayList;
import java.util.List;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanListFragment extends BaseFragment {
    private SwipeRefreshLayout swipe_refresh;
    private RecyclerView recycler;
    private LinearLayoutManager linearLayoutManager;
    private List<MyBluetoothDevice> datas = new ArrayList<>();
    private BleDeviceAdapter bleDeviceAdapter;
    private BleManager bleManager = BleManager.BLE_MANAGER;
    private String uuid_service = "0000fff0-0000-1000-8000-00805f9b34fb";
    private String uuid_write = "0000fff6-0000-1000-8000-00805f9b34fb";
    private String uuid_notification = "0000fff4-0000-1000-8000-00805f9b34fb";
    private String uuid_confirm = "0000fff3-0000-1000-8000-00805f9b34fb";
    private String uuid_notification_des2 = "00002902-0000-1000-8000-00805f9b34fb";
    private Handler handler; //扫描到新设备通知
    //连接成功后跳转到的Activity
    private String className = "";
    //默认权限
    List<PermissionItem> permissonItems = new ArrayList<PermissionItem> ();


    public ScanListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan_list, container, false);
        initView(view);
        initEvents(view);
        className = getArguments().getString("className");
        requestPermission();
        return view;
    }

    @Override
    protected void initView(View view) {
        bleManager.init(uuid_service,uuid_write,uuid_notification,uuid_confirm,uuid_notification_des2,getActivity());
        initHandler();
        swipe_refresh = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        recycler = (RecyclerView)view.findViewById(R.id.recycler);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(linearLayoutManager);
        bleDeviceAdapter = new BleDeviceAdapter(getActivity(),datas,recycler,bleManager);
        recycler.setAdapter(bleDeviceAdapter);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary,R.color.colorAccent,R.color.colorPrimaryDark);
        swipe_refresh.setProgressViewOffset(false,0,(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));

    }

    @Override
    protected void initEvents(View view) {
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bleDeviceAdapter.clearAll();
                bleDeviceAdapter.notifyDataSetChanged();
                swipe_refresh.setRefreshing(false);
                bleManager.startScan();
                Log.d(TAG,"清理数据，重新扫描");
            }
        });
    }

    private void initHandler(){
        ///扫描到新设备
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "msg.what = " + msg.what);
                super.handleMessage(msg);
                if(msg.what == BleConstant.SCANNEWDEVICE){//发现新设备
                    datas = bleManager.getDevicesList();
                    Log.d(TAG,"deviceList = " + datas.toString());
                    MyBluetoothDevice myBluetoothDevice = datas.get(datas.size()-1);
                    Log.d(TAG,"bluetoothDevice.getAddress() = " + myBluetoothDevice.toString());
                    bleDeviceAdapter.clearAll();
                    bleDeviceAdapter.addDatas(datas);
                    bleDeviceAdapter.notifyDataSetChanged();
                }else if(msg.what == BleConstant.STARTCONNECT){
                    Log.d(TAG,"开始连接");
                    Toast.makeText(getActivity(),"开始连接",Toast.LENGTH_SHORT).show();
                }else if(msg.what == BleConstant.CONNECTED){
                    Log.d(TAG,"连接成功，加密认证中...");
                    Toast.makeText(getActivity(),"连接成功，加密认证中...",Toast.LENGTH_SHORT).show();
                    bleManager.stopScan ();
                }else if(msg.what == BleConstant.CONNECTDONE){
                    Log.d(TAG,"认证完成，可跳转");
                    Toast.makeText(getActivity(),"加密认证完成",Toast.LENGTH_SHORT).show();

                    try {
                        Log.d(TAG,"className = " + className);
                        Class<?> activityName = Class.forName(className);

                        Intent intent = new Intent(getActivity(),activityName);
                        startActivity(intent);
                    }catch (Exception e){
                        Log.d(TAG,"跳转失败");
                    }



                }else{
                    Log.d(TAG,"连接状态发生变化； state = " + msg.what);
                }
            }
        };
        bleManager.setBleHandler(handler);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        bleManager.stopScan ();
    }


    public void requestPermission(){
        permissonItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE,getString (R.string.file),R.drawable.permission_ic_storage));
        permissonItems.add(new PermissionItem(android.Manifest.permission.ACCESS_FINE_LOCATION,getString (R.string.location),R.drawable.permission_ic_location));
        HiPermission.create(getActivity())
                .permissions(permissonItems)
                .title(getString (R.string.permission_needed))
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        Log.d(TAG,"用户关闭权限申请");
                    }
                    @Override
                    public void onFinish() {
                        Log.d(TAG,"所有权限申请完成");
                        bleManager.startScan();
                    }
                    @Override
                    public void onDeny(String permisson, int position) {
                        Log.d(TAG, "onDeny");
                        getActivity().finish();
                    }
                    @Override
                    public void onGuarantee(String permisson, int position) {
                        Log.d(TAG, "onGuarantee");
                    }
                });
    }


}
