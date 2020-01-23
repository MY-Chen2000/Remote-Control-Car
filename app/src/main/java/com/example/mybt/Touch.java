package com.example.mybt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Message;
import android.os.Handler;
import android.content.Intent;


public class Touch extends AppCompatActivity {

    public boolean flag = true;
    private BluetoothThread bluetoothThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);


            Button button2 = (Button) findViewById(R.id.button_start);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(Touch.this,"启动", Toast.LENGTH_SHORT).show();

                    sendCommand('B');
                }
            });

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
                            Intent intent = new Intent(Touch.this, MainActivity.class);
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
                Intent intent = new Intent(Touch.this, MainActivity.class);
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
