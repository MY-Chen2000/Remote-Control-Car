package com.example.mybt;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Trace;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class TraceActivity extends AppCompatActivity {


    public boolean flag = true;
    private BluetoothThread bluetoothThread;

    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new LineView(this));
    }

    public class LineView extends View {

        Paint paint = new Paint();

        int x=2;
        float[] pointx=new float[1000];
        float[] pointy=new float[1000];


        Context currentContext=null;

        public LineView(Context context) {
            super(context);
            currentContext=context;
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);
            paint.setDither(true);

        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for(int i=0;i<x-1;i++){
                canvas.drawLine(pointx[i],pointy[i],pointx[i+1],pointy[i+1],paint);
            }
        }


        //   触摸事件
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            //count++;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                pointx[0]=event.getX();
                pointy[0]=event.getY();
                pointx[1]=event.getX();
                pointy[1]=event.getY();
                x=2;
            }
            if(event.getAction() == MotionEvent.ACTION_UP){
                //停止
                x=0;
                sendCommand('P');
                invalidate();
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE){
                pointx[x]=event.getX();
                pointy[x]=event.getY();
                x++;
                count++;
                if(count==5){
                    count=0;
                }
                else{
                    return true;
                }
                float delta_x=pointx[x]-pointx[x-5];
                float delta_y=pointy[x]-pointy[x-5];

                double distance=Math.sqrt(delta_x*delta_x+delta_y*delta_y);
                double cos=delta_x/distance;
                double sin=delta_y/distance;
                double theta=Math.sqrt(2)/2;

                if(sin>-theta&&sin<theta&&cos>theta&&cos<=1){

                    //向右运动
                    sendCommand('R');
//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//
//                            /**
//                             * 延时执行的代码
//                             */
//                            sendCommand('R');
//
//                        }
//                    },500); // 延时1秒

                }
                else if(sin>=theta&&sin<=1&&cos>-theta&&cos<=theta){

                    sendCommand('B');
//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//
//                            /**
//                             * 延时执行的代码
//                             */
//                            sendCommand('B');
//
//                        }
//                    },500); // 延时1秒
                }
                else if(sin>-theta&&sin<theta&&cos>=-1&&cos<=-theta){
                    sendCommand('L');
//                    for (int i=0;i<10000;i++){
//                        if (i==10001){
//                            int a =2;
//                        }
//                    }
//                    //向左运动
//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//
//                            /**
//                             * 延时执行的代码
//                             */
//                            sendCommand('L');
//
//                        }
//                    },500); // 延时1秒
                }
                else {
                    sendCommand('F');
//                    Timer timer = new Timer();
//                    timer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//
//                            /**
//                             * 延时执行的代码
//                             */
//                            sendCommand('F');
//
//                        }
//                    },500); // 延时1秒
                }



                invalidate();

            }

            return true;
        }
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
                        Intent intent = new Intent(TraceActivity.this, MainActivity.class);
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
            Intent intent = new Intent(TraceActivity.this, MainActivity.class);
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
