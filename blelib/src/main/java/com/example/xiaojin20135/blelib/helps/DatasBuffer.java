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
        Log.d(TAG,"firstByte & 0xFF = " + Integer.toHexString(firstByte & 0xFF));
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
//        Log.d(TAG,"组织完整报文 frameBuffer.size() = " + frameBuffer.size());
        //遍历当前缓冲区，判断处于第一个位置的报文的第一个字节是否是帧头
        for(int i=0;i<frameBuffer.size();i++){
            byte firstByte = frameBuffer.get(i)[0];
//            Log.d(TAG, "firstByte = " + Integer.toHexString(firstByte&0xFF));
            if((firstByte & 0x7F) == 0x00){ //如果是帧头，退出循环
                Log.d(TAG,"是帧头，退出循环");
                break;
            }else{
                Log.d(TAG,"不是帧头，移除第一帧");
                frameBuffer.remove(0);
                i = i - 1;
            }
        }
//        Log.d(TAG,"frameBuffer.size() = " + frameBuffer.size());
        if(frameBuffer.size() == 0){
            return;
        }
        //计算报文长度
        byte[] firstFrame = frameBuffer.get(0);
        //当前按照376.1协议计算长度
        int lenbyte = (firstFrame[3] & 0xFF)  + 4;
        Log.d(TAG,"lenbyte = " + lenbyte);
        byte[] frameComplete = new byte[lenbyte];
        int index = 0;
        int circle = (lenbyte / 18);//本次组帧循环次数，根据本次报文长度计算
        if((lenbyte % 18) != 0){
            circle = circle + 1;
        }
        if(circle > frameBuffer.size()){
            Log.d(TAG,"帧丢失");
            return ;
        }
//        Log.d(TAG,"circle = " + circle);
        //剩余字节数
        int leftLen = lenbyte;
        for(index=0;index<circle;index++){
            leftLen = lenbyte - (18 * index);
//            Log.d(TAG,"剩余字节数 leftLen = " + leftLen);
            byte[] currentFrame = frameBuffer.get(index);
            Log.d(TAG,"currentFrame = " + MethodsUtil.METHODS_UTIL.byteToHexString(currentFrame));
            if(index != (frameBuffer.size()-1)){
                for(int j=1;(j<19 && j<leftLen);j++){
//                    Log.d(TAG,"index*18 + (j-1) = " + (index*18 + (j-1)));
//                    Log.d(TAG,"currentFrame["+j+"] = " + Integer.toHexString(currentFrame[j]&0xFF));
                    //i*8 ，当前帧相对于前面帧的偏移量；（j-1）为当前帧内字节偏移量
                    frameComplete[index*18 + (j-1)] = currentFrame[j];
                }
            }else{
                //最后一帧内剩余的字节数
                for(int j=1;j<=leftLen;j++){
                    frameComplete[index*18 + (j-1)] = currentFrame[j];

//                    Log.d(TAG,"1  index*18 + (j-1) = " + (index*18 + (j-1)));
//                    Log.d(TAG,"1  currentFrame["+j+"] = " + currentFrame[j]);
                }
            }
        }
        //移除前circle个分钟呢
        for(int i=0;i<circle;i++){
            frameBuffer.remove(i);
            index--;
        }
//        Log.d(TAG,"index = " + index + " frameBuffer.size() = " + frameBuffer.size());
        Log.d(TAG,"frameComplete = " + MethodsUtil.METHODS_UTIL.byteToHexString(frameComplete));
        if(frameReceivedLis != null){
            frameReceivedLis.receive(frameComplete);
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
            Log.d(TAG,"frameToSend.get(0) = " + MethodsUtil.METHODS_UTIL.byteToHexString(frameToSend.get(0)));
            return frameToSend.get(0);
        }else{
            return null;
        }
    }

    /**
     * 清理当前已经发送的第一帧
     */
    public void clearFirstSended(){
        Log.d(TAG,"in clearFirstSended . frameToSend.size() = " + frameToSend.size());
        if(frameToSend.size() > 0){
            frameToSend.remove(0);
        }
        Log.d(TAG,"in clearFirstSended . frameToSend.size() = " + frameToSend.size());
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
