package com.example.xiaojin20135.bleutils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sendEncrypt(View view){
        Intent intent = new Intent(MainActivity.this, ScanListActivity.class);
        intent.putExtra("className","com.example.xiaojin20135.bleutils.SendTestActivity");
        startActivity(intent);
    }
}
