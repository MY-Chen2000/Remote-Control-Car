package com.example.mybt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;


import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TouchActivity extends AppCompatActivity {

    public boolean flag = true;
    private BluetoothThread bluetoothThread;

    private TextView tv;

    float x=0;
    float y=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);

        Button button2 = (Button) findViewById(R.id.button_ts);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(TouchActivity.this,"stop", Toast.LENGTH_SHORT).show();

                sendCommand('P');
            }
        });
        findView();
    }
    @Override

    public boolean onTouchEvent(MotionEvent event){

        if (MotionEvent.ACTION_DOWN==event.getAction()){
            x=event.getX();

            y=event.getY();

            //tv.setText("您单击的位置是:\nx:"+x+"\n y:"+y);
        }
        if(MotionEvent.ACTION_UP==event.getAction()){
            float x2=event.getX();
            float y2=event.getY();

            float delta_x=x2-x;
            float delta_y=y2-y;

            double distance=Math.sqrt(delta_x*delta_x+delta_y*delta_y);
            double cos=delta_x/distance;
            double sin=delta_y/distance;
            double theta=Math.sqrt(2)/2;
            //tv.setText("起始位置：x:"+x+" y:"+y+"\n终止位置：x"+x2+"y:"+y2);
            if(sin>-theta&&sin<theta&&cos>theta&&cos<=1){
                tv.setText("向右运动");
                sendCommand('R');
            }
            else if(sin>=theta&&sin<=1&&cos>-theta&&cos<=theta){
                tv.setText("向后运动");
                sendCommand('B');
            }
            else if(sin>-theta&&sin<theta&&cos>=-1&&cos<=-theta){
                tv.setText("向左移动");
                sendCommand('L');
            }
            else if(sin>=-1&&sin<=-theta&&cos>-theta&&cos<=theta){
                tv.setText("向前移动");
                sendCommand('F');
            }
        }
        return super.onTouchEvent(event);
    }

    private void findView() {

        tv = (TextView) findViewById(R.id.tv);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothThread = ((MyApplication) getApplication()).getBluetoothThread();
        if (bluetoothThread != null) {
            bluetoothThread.setOutHandler(new Handler() {
                @Override
                public void handleMessage(Message message) {
                    if (message.what == 0x000) {
                        Intent intent = new Intent(TouchActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("startInfo", message.obj.toString());
                        startActivity(intent);
                    }
                }
            });
        }
    }

    public void sendCommand(char command) {
        if (bluetoothThread.isAlive()) {
            Message message = new Message();
            message.what = 0x300;
            message.obj = command;
            bluetoothThread.getInHandler().sendMessage(message);
        }
        else {
            Intent intent = new Intent(TouchActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("startInfo", "蓝牙连接中断");
            startActivity(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

