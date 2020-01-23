package com.example.mybt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.util.Log;


import com.google.gson.Gson;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult ;
import com.iflytek.cloud.SpeechConstant ;
import com.iflytek.cloud.SpeechError ;
import com.iflytek.cloud.SpeechUtility ;
import com.iflytek.cloud.ui.RecognizerDialog ;
import com.iflytek.cloud.ui.RecognizerDialogListener ;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class VoiceActivity extends AppCompatActivity {
    private BluetoothThread bluetoothThread;                        //定义蓝牙线程类
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String TAG = VoiceActivity.class.getCanonicalName();
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private Button voice_btn;
    private boolean hasPermission = false;
    private RecognizerDialog mDialog;
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(VoiceActivity.this, "初始化失败，错误码：" + code, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private static String parseIatResult(String json) {
        if (json == null) {
            return "";
        }
        StringBuilder ret = ret = new StringBuilder();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        if (hasPermission) getPermission();
        else hasPermission = true;

        if (!validateMicAvailability()) {
            Toast.makeText(VoiceActivity.this, "当前麦克风不可用", Toast.LENGTH_SHORT).show();
        }
        SpeechUtility.createUtility(VoiceActivity.this, SpeechConstant.APPID + "=5c067094");
        mDialog = new RecognizerDialog(VoiceActivity.this, mInitListener);
        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                String result = parseIatResult(recognizerResult.getResultString());
                if (result.isEmpty()) {
                    return;
                }

                if (result.contains("前")) {
                    Toast.makeText(VoiceActivity.this, "前进", Toast.LENGTH_SHORT).show();
                    sendCommand('F');
                    // ((MainActivity) getActivity()).forward();
                } else if (result.contains("后")) {
                    Toast.makeText(VoiceActivity.this, "后退", Toast.LENGTH_SHORT).show();
                    sendCommand('B');
                    //((MainActivity) getActivity()).backward();
                } else if (result.contains("停")) {
                    Toast.makeText(VoiceActivity.this, "停止", Toast.LENGTH_SHORT).show();
                    sendCommand('P');
                    // ((MainActivity) getActivity()).stop();
                } else if (result.contains("左")) {
                    if(result.contains("平")){
                        Toast.makeText(VoiceActivity.this, "左平移", Toast.LENGTH_SHORT).show();
                        sendCommand('L');
                    }
                    else {
                        Toast.makeText(VoiceActivity.this, "左转", Toast.LENGTH_SHORT).show();
                        //  ((MainActivity) getActivity()).turnLeft();
                        sendCommand('l');
                    }
                } else if (result.contains("右")) {
                    if(result.contains("平")){
                        Toast.makeText(VoiceActivity.this, "右平移", Toast.LENGTH_SHORT).show();
                        sendCommand('R');
                    }
                    {
                        Toast.makeText(VoiceActivity.this, "右转", Toast.LENGTH_SHORT).show();
                        //((MainActivity) getActivity()).turnRight();
                        sendCommand('r');
                    }
                }
            }

            @Override
            public void onError(SpeechError error) {
                Toast.makeText(VoiceActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        setIatParam();
        voice_btn = (Button)findViewById(R.id.btn_recog);
        voice_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setIatParam() {
        mDialog.setParameter(SpeechConstant.PARAMS, null); // 清空参数
        mDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); // 设置听写引擎
        mDialog.setParameter(SpeechConstant.RESULT_TYPE, "json"); // 设置返回结果格式
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn"); // 设置语言
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin"); // 设置语言区域
        mDialog.setParameter(SpeechConstant.VAD_BOS, "4000"); // 设置语音前端点:初次语音输入前超时时间（毫秒）
        mDialog.setParameter(SpeechConstant.VAD_EOS, "2000"); // 设置语音后端点:已有语音输入后超时时间（毫秒）
        mDialog.setParameter(SpeechConstant.ASR_PTT, "0"); // 设置返回结果是否包含标点符号："0"无,"1"有
    }

    public void getPermission() {
        if (ContextCompat.checkSelfPermission(VoiceActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(VoiceActivity.this, Manifest.permission.RECORD_AUDIO)) {
                showMessageOKCancel("需要打开录音权限才能使用语音控制",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(VoiceActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},
                                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(VoiceActivity.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
        return;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(VoiceActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void initSpeech(final Context context) {
        //Log.d("Debug", "almost here 0");

        //语言中文， 方言：默认
        //Log.d("Debug", "almost here 1");
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if (!isLast) {
                    Log.d("Debug", "almost here 2");
                    String result = recognizerResult.getResultString();
                    Log.d("Debug", "almost here 3");
                    Gson gson = new Gson();
                    StringBuffer stringbuffer = new StringBuffer();
                    //解析语音json
                    ArrayList<Voice.dString> ts = gson.fromJson(result, Voice.class).ts;
                    for (Voice.dString ds : ts) {
                        String word = ds.ds.get(0).s;
                        stringbuffer.append(word);
                    }
                    result = stringbuffer.toString();
                    Toast.makeText(VoiceActivity.this, result, Toast.LENGTH_SHORT).show();
                    processReult(result);
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                int error = speechError.getErrorCode();
                switch (error) {
                    case 20006:
                        Toast.makeText(VoiceActivity.this, "未打开录音权限", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });


    }

    private void processReult(String result) {
        if (VoiceActivity.this != null) {
            if (result.contains("前")) {
                Toast.makeText(VoiceActivity.this, "前进", Toast.LENGTH_SHORT).show();
                sendCommand('F');
                //((MainActivity) getActivity()).forward();
            } else if (result.contains("后")) {
                Toast.makeText(VoiceActivity.this, "后退", Toast.LENGTH_SHORT).show();
                sendCommand('B');
                // ((MainActivity) getActivity()).backward();
            } else if (result.contains("停")) {
                Toast.makeText(VoiceActivity.this, "停止", Toast.LENGTH_SHORT).show();
                sendCommand('P');
                //((MainActivity) getActivity()).stop();
            } else if (result.contains("左")) {
                if (result.contains("平")){
                    Toast.makeText(VoiceActivity.this, "左平移", Toast.LENGTH_SHORT).show();
                    sendCommand('L');
                }
                else {
                    Toast.makeText(VoiceActivity.this, "左转", Toast.LENGTH_SHORT).show();
                    sendCommand('l');
                    // ((MainActivity) getActivity()).turnLeft();
                }
            } else if (result.contains("右")) {
                if(result.contains("平")){
                    Toast.makeText(VoiceActivity.this, "右平移", Toast.LENGTH_SHORT).show();
                    sendCommand('R');
                }
                else {
                    Toast.makeText(VoiceActivity.this, "右转", Toast.LENGTH_SHORT).show();
                    sendCommand('r');
                    //  ((MainActivity) getActivity()).turnRight();
                }
            }
        }
    }

    private boolean validateMicAvailability() {
        Boolean available = true;
        AudioRecord recorder = null;
        try {
            recorder =
                    new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_DEFAULT, 44100);
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                available = false;
            }
            recorder.startRecording();
            if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                recorder.stop();
                available = false;
            }
            recorder.stop();
        } finally {
            if (recorder != null) {
                recorder.release();
            }
        }
        return available;
    }

    public class Voice {

        public ArrayList<dString> ts;

        public class dString {
            public ArrayList<oneString> ds;
        }

        public class oneString {
            public String s;
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
                        Intent intent = new Intent(VoiceActivity.this, MainActivity.class);
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
            Intent intent = new Intent(VoiceActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("startInfo", "蓝牙连接中断");
            startActivity(intent);
        }
    }



}