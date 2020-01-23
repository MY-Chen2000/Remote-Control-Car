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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class joystickActivity extends AppCompatActivity {

    public boolean flag = true;
    private BluetoothThread bluetoothThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new QiuView(this));
/*
        Button button2 = (Button) findViewById(R.id.button_ball);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(joystickActivity.this,"stop", Toast.LENGTH_SHORT).show();

                sendCommand('P');
            }
        });

 */

    }

    public class QiuView extends View {

        Paint paint = new Paint();
        Paint paint2=new Paint();

        private float initX=700,initY=1000;

        private float pointX=initX,pointY=initY;

        private float deltaX=0,deltaY=0;


        private float distance=0;

        private float cos,sin;

        Context currentContext=null;

        public QiuView(Context context) {
            super(context);
            currentContext=context;
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);
            paint.setDither(true);

            paint2.setColor(Color.BLUE);
            paint2.setAlpha(100);
            paint2.setAntiAlias(true);
            paint2.setDither(true);
        }


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawCircle(pointX, pointY, 50, paint);
            canvas.drawCircle(initX,initY,250,paint2);
        }


        //   触摸事件
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE||event.getAction() == MotionEvent.ACTION_DOWN) {
                //point.set(event.getX(), event.getY());
                pointX=event.getX();
                pointY= event.getY();

                deltaX=pointX-initX;
                deltaY=pointY-initY;
                distance=(float)Math.sqrt(deltaX*deltaX+deltaY*deltaY);
                if(distance>200){
                    cos=deltaX/distance;
                    sin=deltaY/distance;
                    if(cos>0.7&&sin>-0.7&&sin<0.7){
                        //向右移动
                        sendCommand('R');

                    }
                    else if(cos<-0.7&&sin>-0.7&&sin<0.7){
                        //向左移动
                        sendCommand('L');

                    }
                    else if(cos>-0.7&&cos<0.7&&sin>0.7){
                        //向后移动
                        sendCommand('B');

                    }
                    else if(cos>-0.7&&cos<0.7&&sin<-0.7){
                        //向前移动
                        sendCommand('F');

                    }

                }
                else{
                    sendCommand('P');
                }
                invalidate();
            }
            if(event.getAction() == MotionEvent.ACTION_UP){
                pointX=initX;
                pointY=initY;
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
                        Intent intent = new Intent(joystickActivity.this, MainActivity.class);
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
            Intent intent = new Intent(joystickActivity.this, MainActivity.class);
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


