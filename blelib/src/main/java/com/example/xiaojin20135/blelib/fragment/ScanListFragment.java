package com.example.xiaojin20135.blelib.fragment;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.print.PrinterId;
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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

import static com.example.xiaojin20135.blelib.helps.BleConstant.UUID_CONFIRM;
import static com.example.xiaojin20135.blelib.helps.BleConstant.UUID_NOTIFICATION;
import static com.example.xiaojin20135.blelib.helps.BleConstant.UUID_NOTIFICATION_DES2;
import static com.example.xiaojin20135.blelib.helps.BleConstant.UUID_SERVICE;
import static com.example.xiaojin20135.blelib.helps.BleConstant.UUID_WRITE;
import static com.example.xiaojin20135.blelib.helps.BleConstant.scanPersisTime;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScanListFragment extends BaseFragment {
    private static final String TAG = "ScanListFragment";
    private SwipeRefreshLayout swipe_refresh;
    private RecyclerView recycler;
    private LinearLayoutManager linearLayoutManager;
    private List<MyBluetoothDevice> datas = new ArrayList<>();
    private BleDeviceAdapter bleDeviceAdapter;
    private BleManager bleManager = BleManager.BLE_MANAGER;



    private String uuid_service;
    private String uuid_write;
    private String uuid_notification;
    private String uuid_confirm;
    private String uuid_notification_des2;
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
        prepareParas();
        View view = inflater.inflate(R.layout.fragment_scan_list, container, false);
        initView(view);
        initEvents(view);
        className = getArguments().getString("className");
        requestPermission();
        return view;
    }

    /*
    * @author lixiaojin
    * create on 2019-11-30 10:52
    * description: 参数准备
    */
    private void prepareParas(){
        uuid_service = getArguments().getString("uuid_service",UUID_SERVICE);
        uuid_write = getArguments().getString("uuid_write",UUID_WRITE);
        uuid_notification = getArguments().getString("uuid_notification",UUID_NOTIFICATION);
        uuid_confirm = getArguments().getString("uuid_confirm",UUID_CONFIRM);
        uuid_notification_des2 = getArguments().getString("uuid_notification_des2",UUID_NOTIFICATION_DES2);
        Log.d(TAG,"**********初始化蓝牙连接参数***********");
        Log.d(TAG,"uuid_service = " + uuid_service);
        Log.d(TAG,"uuid_write = " + uuid_write);
        Log.d(TAG,"uuid_notification = " + uuid_notification);
        Log.d(TAG,"uuid_confirm = " + uuid_confirm);
        Log.d(TAG,"uuid_notification_des2 = " + uuid_notification_des2);
        Log.d(TAG,"**********初始化蓝牙连接参数***********");
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
                startScan();
            }
        });
    }


    /*
    * @author lixiaojin
    * create on 2019-11-30 11:34
    * description: 开始扫描
    */
    private void startScan(){
        //开始扫描
        bleDeviceAdapter.clearAll();
        bleDeviceAdapter.notifyDataSetChanged();
        swipe_refresh.setRefreshing(false);
        bleManager.startScan();
        Log.d(TAG,"清理数据，重新扫描");
        //设置定时器
        Observable.timer(scanPersisTime, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.d(TAG,scanPersisTime + "秒之后停止扫描");
                        bleManager.stopScan();
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
                    showProgress();
//                    Toast.makeText(getActivity(),"开始连接",Toast.LENGTH_SHORT).show();
                }else if(msg.what == BleConstant.CONNECTED){
                    Log.d(TAG,"连接成功，加密认证中...");
//                    Toast.makeText(getActivity(),"连接成功，加密认证中...",Toast.LENGTH_SHORT).show();
                    bleManager.stopScan ();
                }else if(msg.what == BleConstant.CONNECTDONE){
                    Log.d(TAG,"认证完成，可跳转");
                    dismissProgress();
//                    Toast.makeText(getActivity(),"加密认证完成",Toast.LENGTH_SHORT).show();
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


    /*
    * @author lixiaojin
    * create on 2019-11-30 11:52
    * description: 连接成功，跳转到指定页面
    */
    private void connectDone(){

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
                        startScan();
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
