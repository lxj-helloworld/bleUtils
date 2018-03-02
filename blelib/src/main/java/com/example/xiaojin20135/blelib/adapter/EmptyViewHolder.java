package com.example.xiaojin20135.blelib.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.xiaojin20135.blelib.R;

/**
 * Created by xiaojin20135 on 2018-03-01.
 */

public class EmptyViewHolder extends RecyclerView.ViewHolder {
    public TextView empty_TV;
    public EmptyViewHolder(View itemView) {
        super(itemView);
        empty_TV = (TextView)itemView.findViewById(R.id.empty_TV);
    }
}
