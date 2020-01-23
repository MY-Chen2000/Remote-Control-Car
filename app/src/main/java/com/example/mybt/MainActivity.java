package com.example.mybt;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.os.Message;
import android.view.KeyEvent;

import android.widget.TextView;
import android.widget.ToggleButton;


public class MainActivity extends AppCompatActivity {

    private BluetoothThread bluetoothThread;                        //定义蓝牙线程类
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //跳转到图像回传活动
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1){
                Intent intent1 = new Intent(MainActivity.this, Camera.class);
                startActivity(intent1);
            }
        });

        //跳转到悬浮球活动
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2){
                Intent intent2 = new Intent(MainActivity.this, joystickActivity.class);
                startActivity(intent2);
            }
        });

        //跳转到语音控制活动
        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v3){
                Intent intent3 = new Intent(MainActivity.this, VoiceActivity.class);
                startActivity(intent3);
            }
        });

        //跳转到重力感应控制
        Button button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v4){
                Intent intent4 = new Intent(MainActivity.this, GravityActivity.class);
                startActivity(intent4);
            }
        });

        //跳转到实施手势模式
        Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v5){
                Intent intent5 = new Intent(MainActivity.this, TouchActivity.class);
                startActivity(intent5);
            }
        });

        //跳转到实施路径模式
        Button button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v6){
                Intent intent6 = new Intent(MainActivity.this, TraceActivity.class);
                startActivity(intent6);
            }
        });



       preferences = getSharedPreferences("com.example.my3cprj", MODE_PRIVATE);
       editor = preferences.edit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothThread = ((MyApplication) getApplication()).getBluetoothThread();                //获取本机蓝牙线程

        if (bluetoothThread != null) {
            bluetoothThread.setOutHandler(new Handler() {
                @Override
                public void handleMessage(Message message) {
                    if (message.what == 0x000) {
                        ((TextView) findViewById(R.id.Status)).setText(message.obj.toString());
                        findViewById(R.id.bluetoothProgressBar).setVisibility(View.GONE);
                        if (message.obj.toString().equals("蓝牙连接成功")) {                      //连接蓝牙并改变按钮状态
                            ((ToggleButton) findViewById(R.id.bluetoothButton)).setChecked(true);
                        } else {
                            ((ToggleButton) findViewById(R.id.bluetoothButton)).setChecked(false);
                        }
                    }
                }
            });
        }

        ((TextView) findViewById(R.id.Status)).setText("");
        try {
            String startInfo = getIntent().getStringExtra("startInfo");
            getIntent().removeExtra("startInfo");
            ((TextView) findViewById(R.id.Status)).setText(startInfo);
            if (startInfo.equals("蓝牙连接中断")) {
                ((ToggleButton) findViewById(R.id.bluetoothButton)).setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        if (bluetoothThread != null) {
            if (bluetoothThread.isAlive()) {                                                     //退出活动时销毁蓝牙线程
                if (bluetoothThread.getInHandler() != null) {
                    Message message = new Message();
                    message.what = 0x301;
                    bluetoothThread.getInHandler().sendMessage(message);
                }
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {                                      //按下返回键时销毁蓝牙线程
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (bluetoothThread != null) {
                if (bluetoothThread.isAlive()) {
                    if (bluetoothThread.getInHandler() != null) {
                        Message message = new Message();
                        message.what = 0x301;
                        bluetoothThread.getInHandler().sendMessage(message);
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void onBluetoothToggleButtonClick(View source) {                                       //根据蓝牙连接状态改变按钮状态
        ToggleButton toggleButton = (ToggleButton) source;
        if (toggleButton.isChecked()) {
            toggleButton.setChecked(false);
            if (bluetoothThread != null) {
                if (bluetoothThread.isAlive()) {
                    return;
                }
            }
            bluetoothThread = new BluetoothThread(new Handler() {
                @Override
                public void handleMessage(Message message) {
                    if (message.what == 0x000) {
                        ((TextView) findViewById(R.id.Status)).setText(message.obj.toString());
                        findViewById(R.id.bluetoothProgressBar).setVisibility(View.GONE);
                        if (message.obj.toString().equals("蓝牙连接成功")) {
                            ((ToggleButton) findViewById(R.id.bluetoothButton)).setChecked(true);
                        } else {
                            ((ToggleButton) findViewById(R.id.bluetoothButton)).setChecked(false);
                        }
                    }
                }
            });
            ((MyApplication) getApplication()).setBluetoothThread(bluetoothThread);
            ((TextView) findViewById(R.id.Status)).setText("正在连接蓝牙");
            findViewById(R.id.bluetoothProgressBar).setVisibility(View.VISIBLE);
            bluetoothThread.start();
        } else {
            toggleButton.setChecked(true);
            if (bluetoothThread != null) {
                if (bluetoothThread.isAlive()) {
                    if (bluetoothThread.getInHandler() != null) {
                        Message message = new Message();
                        message.what = 0x301;
                        bluetoothThread.getInHandler().sendMessage(message);
                    }
                }
            }
        }
    }
}
