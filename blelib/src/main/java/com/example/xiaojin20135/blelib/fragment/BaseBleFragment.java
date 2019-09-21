package com.example.xiaojin20135.blelib.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xiaojin20135.basemodule.fragment.base.BaseFragment;
import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.FrameReceivedLis;
import com.example.xiaojin20135.blelib.R;
import com.example.xiaojin20135.blelib.helps.BleConstant;
import com.example.xiaojin20135.blelib.helps.MethodsUtil;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/*
* @author lixiaojin
* create on 2019-09-21 15:29
* description: 基本的蓝牙通信片段
*/
public abstract class BaseBleFragment extends BaseFragment {
    public BaseBleFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        TextView textView = new TextView(getActivity());
        return textView;
    }

    /*
    * @author lixiaojin
    * create on 2019-09-21 15:30
    * description: 发送
    */
    public void send(byte[] frame){
        BleManager.BLE_MANAGER.sendSliceData(frame,frameReceivedLis);
    }

    /*
    * @author lixiaojin
    * create on 2019-09-21 15:30
    * description: 接收监听
    */
    FrameReceivedLis frameReceivedLis = new FrameReceivedLis(){
        @Override
        public void receive(byte[] frame) {
            //如果报文内容为空，不处理
            if(frame == null || frame.length == 0){
                return;
            }
            //将报文内容从子线程分发到主线程
            Observable.just(frame)
                   .subscribeOn(AndroidSchedulers.mainThread())
                   .subscribe(new Consumer<byte[]>() {
                       @Override
                       public void accept(byte[] bytes) throws Exception {
                           receiveFrame(bytes);
                       }
                   });
        }
    };

    public abstract void receiveFrame(byte[] frame);

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
}
