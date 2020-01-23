package com.example.mybt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BluetoothThread extends Thread {
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> bluetoothDeviceSet;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private BufferedOutputStream bluetoothSocketBufferedOutputStream;
    private Handler inHandler;
    private Handler outHandler;
    private Message outMessage;

    public BluetoothThread(Handler outHandler) {                 //创建蓝牙线程
        this.outHandler = outHandler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //使用BluetoothAdapter类，在Android设备上查找周边的蓝牙设备然后配对绑定
    }

    public void setOutHandler(Handler outHandler) {
        this.outHandler = outHandler;
    }

    public Handler getInHandler() {
        return inHandler;
    }

    @Override
    public void run() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        if (bluetoothDeviceSet.size() != 0) {                                  //判断是否存在蓝牙设备
            bluetoothDevice = bluetoothDeviceSet.iterator().next();
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();                                      //连接蓝牙
                bluetoothSocketBufferedOutputStream = new BufferedOutputStream(bluetoothSocket.getOutputStream());
            } catch (Exception e) {                                              //捕捉连接异常并返回异常信息
                e.printStackTrace();
                outMessage = new Message();
                outMessage.what = 0x000;
                outMessage.obj = "蓝牙连接错误";                              //发送连接失败message
                outHandler.sendMessage(outMessage);
                return;
            }
            outMessage = new Message();
            outMessage.what = 0x000;
            outMessage.obj = "蓝牙连接成功";
            outHandler.sendMessage(outMessage);                               //发送连接成功message

            Looper.prepare();
            inHandler = new Handler() {
                @Override
                public void handleMessage(Message inMessage) {
                    try {
                        if (inMessage.what == 0x300) {
                            String command = inMessage.obj.toString();             //蓝牙uuid码
                            for (int i = 0; i < command.length(); i++) {
                                bluetoothSocketBufferedOutputStream.write(command.charAt(i));
                            }
                            bluetoothSocketBufferedOutputStream.flush();      //刷新输出缓存
                        } else if (inMessage.what == 0x301) {
                            getLooper().quit();
                        }
                    } catch (Exception e) {
                        getLooper().quit();
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();                                                         //循环取出message并交给handler
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outMessage = new Message();
            outMessage.what = 0x000;
            outMessage.obj = "蓝牙连接中断";                                 //发送连接中断message
            outHandler.sendMessage(outMessage);
        } else {
            Message message = new Message();
            message.what = 0x000;
            message.obj = "蓝牙连接错误";
            outHandler.sendMessage(message);                                    //发送错误message
        }

        System.out.println("******** bluetoothThread quit ********");

    }

}
