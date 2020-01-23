package com.example.mybt;



import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

public class GravityActivity extends AppCompatActivity {

    public boolean flag = true;
    private BluetoothThread bluetoothThread;


    private SensorEventListener lsn;
    private SensorManager sensorMgr;
    Sensor sensor;
    TextView textX = null;
    TextView textY = null;
    TextView textZ = null;
    private float x, y, z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity);
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        textX = (TextView) findViewById(R.id.textx);
        textY = (TextView) findViewById(R.id.texty);
        textZ = (TextView) findViewById(R.id.textz);

        lsn = new SensorEventListener() {
            public void onSensorChanged(SensorEvent e) {
                x = e.values[0];
                y = e.values[1];
                z = e.values[2];
                if(x>3){
                    setTitle("向左运动");
                    //Toast.makeText(GravityActivity.this,"left", Toast.LENGTH_SHORT).show();

                   // sendCommand('L');
                }
                else if(x<-3){
                    setTitle("向右运动");
                    //Toast.makeText(GravityActivity.this,"right", Toast.LENGTH_SHORT).show();

                   // sendCommand('R');
                }
                else if(y<-3){
                    setTitle("向前运动");


                    //sendCommand('F');
                }
                else if(y>3){
                    setTitle("向后运动");
                    //sendCommand('B');
                }
                else{
                    setTitle("静止");
                   // sendCommand('P');
                }
                //setTitle("x=" + (int) x + "," + "y=" + (int) y + "," + "z="+ (int) z);
                textX.setText("x=" + (int) x);
                textY.setText("y=" + (int) y);
                textZ.setText("z=" + (int) z);

            }
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }
        };
        // 注册listener，第三个参数是检测的精确度
        sensorMgr.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_GAME);
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
                        Intent intent = new Intent(GravityActivity.this, MainActivity.class);
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
            Intent intent = new Intent(GravityActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("startInfo", "蓝牙连接中断");
            startActivity(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mOrientationListener.disable();
        sensorMgr.unregisterListener(lsn);
    }

}
