package com.example.xiaojin20135.blelib.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xiaojin20135.blelib.BleManager;
import com.example.xiaojin20135.blelib.R;
import com.example.xiaojin20135.blelib.bean.MyBluetoothDevice;

import java.util.List;

/**
 * Created by xiaojin20135 on 2018-03-01.
 */

public class BleDeviceAdapter extends RecyclerView.Adapter implements View.OnClickListener{
    private static final String TAG = "BleDeviceAdapter";
    private Activity activity;
    private List<MyBluetoothDevice> datas;
    private RecyclerView recyclerView;
    private BleManager bleManager;
    private static final int EMPTY_VIEW = 1;


    public BleDeviceAdapter(Activity activity,List<MyBluetoothDevice> datas,RecyclerView recyclerView,BleManager bleManager){
        this.activity = activity;
        this.datas = datas;
        this.recyclerView = recyclerView;
        this.bleManager = bleManager;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == EMPTY_VIEW){
            return new EmptyViewHolder(LayoutInflater.from(activity).inflate(R.layout.nodata,parent,false));
        }else{
            View view = LayoutInflater.from(activity).inflate(R.layout.ble_item_layout,parent,false);
            MyViewHolder myViewHolder = new MyViewHolder(view);
            view.setOnClickListener(this);
            return myViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof BleDeviceAdapter.MyViewHolder){
            MyViewHolder myViewHolder = (MyViewHolder)holder;
            MyBluetoothDevice myBluetoothDevice = datas.get(position);
            //名称
            myViewHolder.name_TV.setText(myBluetoothDevice.getBluetoothDevice().getName());
            //mac地址
            myViewHolder.mac_address_TV.setText(myBluetoothDevice.getBluetoothDevice().getAddress());
            //信号强度
            myViewHolder.rssi_TV.setText(myBluetoothDevice.getRssi() + "");
            Log.d(TAG,"myBluetoothDevice = " + myBluetoothDevice.toString());

        }
    }

    @Override
    public int getItemCount() {
        return datas.size() > 0 ? datas.size() : 1;
    }

    @Override
    public void onClick(View v) {
        //获取点击位置索引
        int position = recyclerView.getChildAdapterPosition(v);
        Log.d(TAG,"position = " + position);
        bleManager.setmDevice(datas.get(position).getBluetoothDevice());
        bleManager.startConnect();
    }

    @Override
    public int getItemViewType(int position) {
        if(datas.size() == 0){
            return EMPTY_VIEW;
        }else{
            return super.getItemViewType(position);
        }
    }
    /**
     * 增加
     * @param data
     */
    public void addDatas(List data){
        datas.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 清空所有数据
     */
    public void clearAll(){
        datas.clear();
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView name_TV;
        private TextView mac_address_TV;
        private TextView rssi_TV;

        public MyViewHolder(View itemView) {
            super(itemView);
            name_TV = (TextView) itemView.findViewById(R.id.name_TV);
            mac_address_TV = (TextView) itemView.findViewById(R.id.mac_address_TV);
            rssi_TV = (TextView) itemView.findViewById(R.id.rssi_TV);
        }
    }

}
