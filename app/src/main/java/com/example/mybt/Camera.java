package com.example.mybt;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Camera extends AppCompatActivity {
    RevImageThread revImageThread;
    public static ImageView image;
    private static Bitmap bitmap;
    private MyHandler handler;
    static int  number=0;

    Bitmap srcBitmap;

    private BluetoothThread bluetoothThread;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        image=(ImageView)findViewById(R.id.imageView1);
        handler = new MyHandler();
        revImageThread = new RevImageThread(handler);
        new Thread(revImageThread).start();

        Button take_photo = (Button) findViewById(R.id.photo);
        take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v5){
                saveBmp2Gallery(Camera.this,bitmap,"photo");
            }
        });

        Button up= (Button) findViewById(R.id.up);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand('F');
                //Toast.makeText(Camera.this,"前进",Toast.LENGTH_LONG).show();
            }
        });

        Button down= (Button) findViewById(R.id.down);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand('B');
                //Toast.makeText(Camera.this,"后退",Toast.LENGTH_LONG).show();
            }
        });

        Button left= (Button) findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand('l');
                //Toast.makeText(Camera.this,"左转",Toast.LENGTH_LONG).show();
            }
        });

        Button right= (Button) findViewById(R.id.right);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand('r');
                //Toast.makeText(Camera.this,"右转",Toast.LENGTH_LONG).show();
            }
        });

        Button stop= (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand('P');
                //Toast.makeText(Camera.this,"刹车",Toast.LENGTH_LONG).show();
            }
        });

    }

    static class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    bitmap = (Bitmap)msg.obj;
                    image.setImageBitmap(bitmap);
                    super.handleMessage(msg);
                    break;
                case 2:
                    // editText_2.setText(msg.obj.toString());
                    //  stringBuffer.setLength(0);
                    break;
            }
        }
    }

    public static void saveBmp2Gallery(Context context, Bitmap bmp, String picName) {

        String fileName = null;
        number++;
        picName=picName+String.valueOf(number);
        //系统相册目录
        String galleryPath= Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                +File.separator+"Camera"+File.separator;


        // 声明文件对象
        File file = null;
        // 声明输出流
        FileOutputStream outStream = null;

        try {
            // 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
            file = new File(galleryPath, picName+ ".jpg");

            // 获得文件相对路径
            fileName = file.toString();
            // 获得输出流，如果文件中有内容，追加内容
            outStream = new FileOutputStream(fileName);
            if (null != outStream) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
            }

        } catch (Exception e) {
            e.getStackTrace();
        }finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //通知相册更新
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bmp,
                fileName, null);

        Uri uri = Uri.fromFile(file);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        Toast.makeText(context,"save photo",Toast.LENGTH_SHORT);
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
                        Intent intent = new Intent(Camera.this, MainActivity.class);
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
            Intent intent = new Intent(Camera.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("startInfo", "蓝牙连接中断");
            startActivity(intent);
        }
    }

}