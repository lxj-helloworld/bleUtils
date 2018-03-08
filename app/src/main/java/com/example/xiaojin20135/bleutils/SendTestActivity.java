package com.example.xiaojin20135.bleutils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.FrameReceivedLis;
import com.example.xiaojin20135.blelib.helps.DatasBuffer;
import com.example.xiaojin20135.blelib.helps.MethodsUtil;

public class SendTestActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "SendTestActivity";
    private TextView frame_TV;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                frame_TV.setText(frame_TV.getText() + msg.obj.toString() +"\r\t");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_test);
        initEvents();
    }

    private void initEvents(){
        frame_TV = (TextView)findViewById(R.id.frame_TV);
    }

    @Override
    public void onClick(View v) {

    }

    public void sendTest(View v){
        byte[] datas = {(byte)0x68,(byte)0x4a,(byte)0x00,(byte)0x4a,(byte)0x00,(byte)0x68,(byte)0x5b,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0x04,(byte)0x0c,(byte)0xe4,(byte)0x00,(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x04,(byte)0x49,(byte)0x22,(byte)0x11,(byte)0x22,(byte)0x00,(byte)0xef,(byte)0x16};
        BleManager.BLE_MANAGER.sendSliceData(datas);
        DatasBuffer.DATAS_BUFFER.setFrameReceivedLis(new FrameReceivedLisImpl(){
            @Override
            public void receive(byte[] frame) {
                super.receive(frame);
                Log.d(TAG, "frame = " + MethodsUtil.METHODS_UTIL.byteToHexString(frame));
                Message message = new Message();
                message.what = 1;
                message.obj = MethodsUtil.METHODS_UTIL.byteToHexString(frame);
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 打开通道
     * @param view
     */
    public void openSignal(View view){
        byte[] datas = {(byte)0xAA,0x00,0x09,0x07,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x16};
        BleManager.BLE_MANAGER.sendSliceData(datas);
        DatasBuffer.DATAS_BUFFER.setFrameReceivedLis(new FrameReceivedLisImpl(){
            @Override
            public void receive(byte[] frame) {
                super.receive(frame);
                Log.d(TAG, "frame = " + MethodsUtil.METHODS_UTIL.byteToHexString(frame));
                Message message = new Message();
                message.what = 1;
                message.obj = MethodsUtil.METHODS_UTIL.byteToHexString(frame);
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 开始检测
     * @param view
     */
    public void startWork(View view){
        byte[] datas = {(byte)0xAA,0x00,0x09,0x07,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)0x80,0x16};
        BleManager.BLE_MANAGER.sendSliceData(datas);
        DatasBuffer.DATAS_BUFFER.setFrameReceivedLis(new FrameReceivedLisImpl(){
            @Override
            public void receive(byte[] frame) {
                super.receive(frame);
                Log.d(TAG, "frame = " + MethodsUtil.METHODS_UTIL.byteToHexString(frame));
                Message message = new Message();
                message.what = 1;
                message.obj = MethodsUtil.METHODS_UTIL.byteToHexString(frame);
                handler.sendMessage(message);
            }
        });
    }

    /**
     * 停止检测
     * @param view
     */
    public void stopWork(View view){
        byte[] datas = {(byte)0xAA,0x00,0x09,0x07,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x16};
        BleManager.BLE_MANAGER.sendSliceData(datas);
        DatasBuffer.DATAS_BUFFER.setFrameReceivedLis(new FrameReceivedLisImpl(){
            @Override
            public void receive(byte[] frame) {
                super.receive(frame);
                Log.d(TAG, "frame = " + MethodsUtil.METHODS_UTIL.byteToHexString(frame));
                Message message = new Message();
                message.what = 1;
                message.obj = MethodsUtil.METHODS_UTIL.byteToHexString(frame);
                handler.sendMessage(message);
            }
        });
    }



    class FrameReceivedLisImpl implements FrameReceivedLis{
        @Override
        public void receive(byte[] frame) {

        }
    }

}
