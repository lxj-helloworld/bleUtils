package com.example.xiaojin20135.blelib.helps;

import android.util.Log;

import com.example.xiaojin20135.blelib.FrameReceivedLis;

import java.util.ArrayList;
import java.util.Arrays;
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
    private volatile List<byte[]>  frameBuffer = new ArrayList<>();
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
    public synchronized void addFrame(byte[] frame){
        if(frame == null || frame.length == 0){
            return;
        }
        //获取第一个字节，判断是新报文的开头还是一个报文的结束
        byte firstByte = frame[0];
        //如果是一个报文的第一帧，清空当前缓冲区数据
        if((firstByte & 0xFF) == 0x00){
            frameBuffer.clear();
        }
        frameBuffer.add(frame);
        //判断第一个字节是否是结束帧，如果是，组织一个完整的报文
        if(((firstByte & 0xFF) & 0x80) == 0x80){
            //复制数组，不包含第一个元素
            byte[] resultArr = Arrays.copyOfRange(frameBuffer.get(0),1,frameBuffer.get(0).length);
            Log.d(TAG,"第0次拼接 resultArr = " + MethodsUtil.METHODS_UTIL.byteToHexString(resultArr));
            for(int i=1;i<frameBuffer.size();i++){
                byte[] tempArr = Arrays.copyOfRange(frameBuffer.get(i),1,frameBuffer.get(i).length);
                resultArr = Arrays.copyOf(resultArr,resultArr.length + tempArr.length);
                System.arraycopy(tempArr,0,resultArr,resultArr.length - tempArr.length,tempArr.length);
                Log.d(TAG,"第"+i+"次拼接 resultArr = " + MethodsUtil.METHODS_UTIL.byteToHexString(resultArr));
            }
            frameReceivedLis.receive(resultArr);
            frameBuffer.clear();
        }
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
            return frameToSend.get(0);
        }else{
            return null;
        }
    }

    /**
     * 清理当前已经发送的第一帧
     */
    public void clearFirstSended(){
        if(frameToSend.size() > 0){
            frameToSend.remove(0);
        }
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
