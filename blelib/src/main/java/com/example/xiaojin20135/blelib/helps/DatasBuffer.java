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

    //收到报文后，需要通知的监听接口
    private FrameReceivedLis frameReceivedLis = null;


    DatasBuffer(){
        frameBuffer.clear();
        frameBufferComplete.clear();
    }

    /**
     * 新增一帧数据
     * @param frame
     */
    public void addFrame(byte[] frame){
        //获取第一个字节，判断是新报文的开头还是一个报文的结束
        byte firstByte = frame[0];
        //如果是一个报文的第一帧，清空当前缓冲区数据
        if((firstByte & 0xFF) == 0x00){
            frameBuffer.clear();
        }
        frameBuffer.add(frame);
        //判断第一个字节是否是结束帧，如果是，组织一个完整的报文
        if((firstByte & 0xFF & 0x80) == 0x80){
            makeACompleteFrame();
        }
    }
    /**
     * 返回一帧完整的报文
     */
    public void makeACompleteFrame(){
        //遍历当前缓冲区，判断处于第一个位置的报文的第一个字节是否是帧头
        for(int i=0;i<frameBuffer.size();i++){
            byte firstByte = frameBuffer.get(i)[0];
            if((firstByte & 0xFF) == 0x00){ //如果是帧头，退出循环
                break;
            }else{
                frameBuffer.remove(0);
                i = i - 1;
            }
        }
        if(frameBuffer.size() == 0){
            return;
        }
        //计算报文长度
        byte[] firstFrame = frameBuffer.get(0);
        //当前按照376.1协议计算长度
        int lenbyte = ((firstFrame[2] & 0xFF) >> 2) + ((firstFrame[3] & 0xFF) << 6) + 8;
        Log.d(TAG,"lenbyte = " + lenbyte);
        byte[] frameComplete = new byte[lenbyte];
        int index = 0;
        for(int i=0;i<frameBuffer.size();i++){
            byte[] currentFrame = frameBuffer.get(i);
            if(i != (frameBuffer.size()-1)){
                for(int j=1;j<19;j++){
                    //i*8 ，当前帧相对于前面帧的偏移量；（j-1）为当前帧内字节偏移量
                    frameComplete[i*18 + (j-1)] = currentFrame[j];
                }
            }else{
                //最后一帧内剩余的字节数
                int leftBytes = lenbyte - (18 * i);
                for(int j=1;j<=leftBytes;j++){
                    frameComplete[i*18 + (j-1)] = currentFrame[j];
                }
            }
        }
        Log.d(TAG,"frameComplete = " + MethodsUtil.METHODS_UTIL.byteToHexString(frameComplete));
        if(frameReceivedLis != null){
            frameReceivedLis.receive(frameComplete);
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
}
