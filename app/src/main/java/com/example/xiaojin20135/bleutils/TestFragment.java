package com.example.xiaojin20135.bleutils;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.xiaojin20135.blelib.fragment.BaseBleFragment;
import com.example.xiaojin20135.blelib.helps.MethodsUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class TestFragment extends BaseBleFragment {
    private Button send_btn;
    private TextView frame_TV;

    public TestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        initView(view);
        initEvents(view);
        return view;
    }

    @Override
    protected void initView(View view) {
        send_btn = (Button)view.findViewById(R.id.send_btn);
        frame_TV = (TextView)view.findViewById(R.id.frame_TV);
    }

    @Override
    protected void initEvents(View view) {
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    public void receiveFrame(byte[] frame) {
        frame_TV.append(MethodsUtil.METHODS_UTIL.byteToHexString(frame) + "\r\n");
    }

    /*
    * @author lixiaojin
    * create on 2019-09-21 15:36
    * description: 发送数据
    */
    private void sendMessage() {
        byte[] tempArr = new byte[100];
        for(int i=0;i<tempArr.length;i++){
            tempArr[i] = (byte)i;
        }
        send(tempArr);
    }



}
