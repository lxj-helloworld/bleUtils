package com.example.xiaojin20135.blelib.helps;

import android.util.Log;

import com.example.xiaojin20135.blelib.FrameReceivedLis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaojin20135 on 2018-03-01.
 * 存放从蓝牙设备收到的每帧数据
 *
 * 关于帧缓存
 * 每收到一帧报文，先判断是否是报文的第一帧，如果是，清空当前帧缓存，将当前第一帧加入，如果是最后一帧，将当前缓冲区的帧，组织为一条完整的报文，如果收到的帧既不是第一帧也不是最后一帧，加入；
 *
 * 关于当前报文缓存
 * 将当前帧缓存组织为一条完整的报文后，将新报文加入该队列中，并发出通知
 */

public enum DatasBuffer {
    DATAS_BUFFER;
    private static final String TAG = "DatasBuffer";
    //当前帧缓存
    private List<byte[]> frameBuffer = new ArrayList<>();
    //当前完整报文缓存
    private List<byte[]> frameBufferComplete = new ArrayList<>();
    //当前需要发送的报文
    private List<byte[]> frameToSend = new ArrayList<>();

    //收到报文后，需要通知的监听接口
    private FrameReceivedLis frameReceivedLis = null;


    DatasBuffer(){
        frameBuffer.clear();
        frameBufferComplete.clear();
        frameToSend.clear();
    }

    /**
     * 新增一帧数据
     * @param frame
     */
    public void addFrame(byte[] frame){
        //获取第一个字节，判断是新报文的开头还是一个报文的结束
        byte firstByte = frame[0];
//        Log.d(TAG,"firstByte & 0xFF = " + Integer.toHexString(firstByte & 0xFF));
        //如果是一个报文的第一帧，清空当前缓冲区数据
        if((firstByte & 0xFF) == 0x00){
            frameBuffer.clear();
        }
        frameBuffer.add(frame);
        //判断第一个字节是否是结束帧，如果是，组织一个完整的报文
        if(((firstByte & 0xFF) & 0x80) == 0x80){
            makeACompleteFrame();
        }
    }
    /**
     * 返回一帧完整的报文
     */
    public synchronized void  makeACompleteFrame(){
        //遍历当前缓冲区，判断处于第一个位置的报文的第一个字节是否是帧头
        for(int i=0;i<frameBuffer.size();i++){
            byte firstByte = frameBuffer.get(i)[0];
//            Log.d(TAG, "firstByte = " + Integer.toHexString(firstByte&0xFF));
            if((firstByte & 0x7F) == 0x00){ //如果是帧头，退出循环
//                Log.d(TAG,"是帧头，退出循环");
                break;
            }else{
//                Log.d(TAG,"不是帧头，移除第一帧");
                frameBuffer.remove(0);
                i = i - 1;
            }
        }
        if(frameBuffer.size() == 0){
            return;
        }
        int circle = this.frameBuffer.size();
        byte[] allFrame = new byte[circle * 18];
        Log.d(TAG,"allFrame.length = " + allFrame.length);
        for(int i=0;i<circle;i++){
            byte[] frame = frameBuffer.get(i);
            for(int j=0;j<18;j++){
                allFrame[j+i*18] = frame[j+1];
            }
        }
        Log.d(TAG,"allFrame = " + MethodsUtil.METHODS_UTIL.byteToHexString(allFrame));
        //拆分，分发
        int left = allFrame.length;
        int fromIndex = 0;
        while(left > 0){
            Log.d(TAG,"2 + fromIndex = " + (2 + fromIndex));
            if((2 + fromIndex) < allFrame.length){
                int lenByte = (allFrame[2 + fromIndex] & 0xFF) + 4;
                Log.d(TAG,"lenByte = " + lenByte);
                if(lenByte < left && lenByte > 4){
                    byte[] frameComplete = new byte[lenByte];
                    for(int i=0;i<lenByte;i++){
                        frameComplete[i] = allFrame[i+fromIndex];
                    }
                    fromIndex = fromIndex + lenByte;
                    left = left - lenByte;
                    if(frameReceivedLis != null){
                        frameReceivedLis.receive(frameComplete);
                    }
                }else{
//                    Log.d(TAG,"lenByte < left = " + (lenByte < left));
                    break;
                }
            }else{
                Log.d(TAG,"没有更多数据");
                break;
            }
        }
        frameBuffer.clear();
    }
    /**
     * 清理当前需要发送的报文
     */
    public void clearToSend(){
        frameToSend.clear();
    }

    /**
     * 新增一帧需要发送的报文
     * @param frame
     */
    public void addToSend(byte[] frame){
        frameToSend.add(frame);
    }

    /**
     * 获取当前需要发送的第一帧
     * @return
     */
    public byte[] getFirstToSend(){
        Log.d(TAG,"in getFirstToSend. frameToSend.size() = " + frameToSend.size());
        if(frameToSend.size() > 0){
//            Log.d(TAG,"frameToSend.get(0) = " + MethodsUtil.METHODS_UTIL.byteToHexString(frameToSend.get(0)));
            return frameToSend.get(0);
        }else{
            return null;
        }
    }

    /**
     * 清理当前已经发送的第一帧
     */
    public void clearFirstSended(){
//        Log.d(TAG,"in clearFirstSended . frameToSend.size() = " + frameToSend.size());
        if(frameToSend.size() > 0){
            frameToSend.remove(0);
        }
//        Log.d(TAG,"in clearFirstSended . frameToSend.size() = " + frameToSend.size());
    }


    public List<byte[]> getFrameBuffer() {
        return frameBuffer;
    }

    public void setFrameBuffer(List<byte[]> frameBuffer) {
        this.frameBuffer = frameBuffer;
    }

    public FrameReceivedLis getFrameReceivedLis() {
        return frameReceivedLis;
    }

    public void setFrameReceivedLis(FrameReceivedLis frameReceivedLis) {
        this.frameReceivedLis = frameReceivedLis;
    }

    public List<byte[]> getFrameToSend() {
        return frameToSend;
    }

    public void setFrameToSend(List<byte[]> frameToSend) {
        this.frameToSend = frameToSend;
    }
}
