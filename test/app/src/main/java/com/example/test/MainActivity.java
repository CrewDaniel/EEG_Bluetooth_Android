package com.example.test;

//Display import
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

//Bluetooth import
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //Bluetooth var
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    //Bluetooth static
    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    ProgressBar mprogressBar;
    ProgressBar mprogressBar2;
    ProgressBar mprogressBar3;
    TextView tv;
    TextView tv2;
    TextView tv3;
    TextView tvData;
    Button top_btn;
    Button bottom_btn;

    String[] token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_main);




        top_btn = findViewById(R.id.top_btn);
        bottom_btn = findViewById(R.id.bottom_btn);

        mprogressBar = findViewById(R.id.circular_progress_bar);
        mprogressBar2 = findViewById(R.id.circular_progress_bar2);
        mprogressBar3 = findViewById(R.id.circular_progress_bar3);

        int val = 0;
        int val2 = 0;
        int val3 = 0;
        tv = findViewById(R.id.textView);
        tv2 = findViewById(R.id.textView2);
        tv3 = findViewById(R.id.textView3);
        tvData = findViewById(R.id.data);
        tv.setText(val + "%");
        tv2.setText(val2 + "%");
        tv3.setText(val3 + "%");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bottom_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });

        mBluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    tvData.setText(readMessage);
                    StringTokenizer st = new StringTokenizer(readMessage, " ");
                    List<String> list = new ArrayList<>();
                    double rv, rv2;
                    int nv, nv2, nv3, ratio;
                    while(st.hasMoreTokens()) {
                        list.add(st.nextToken());
                    }
                    rv = Double.parseDouble(list.get(0))*10;
                    rv2 = Double.parseDouble(list.get(1))*10;
                    nv = (int)rv;
                    nv2 = (int)rv2;
                    nv3 = (int)Double.parseDouble(list.get(2));
                    ratio = (int)(rv/rv2*100);

                    progressReset(nv/2, nv2/2, ratio);
                }
            }
        };
    }



    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }
    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[128];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    void progressReset(int val, int val2, int val3) {
        ObjectAnimator anim = ObjectAnimator.ofInt(mprogressBar, "progress", 0, val3); //value: 0~100
        anim.setDuration(100);
        anim.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator anim2 = ObjectAnimator.ofInt(mprogressBar2, "progress", 0, val); //value: 0~100
        anim2.setDuration(100);
        anim2.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator anim3 = ObjectAnimator.ofInt(mprogressBar3, "progress", 0, val2); //value: 0~100
        anim3.setDuration(100);
        anim3.setInterpolator(new DecelerateInterpolator());



        anim.start();
        anim2.start();
        anim3.start();

        tv.setText(val3+ "%");
        tv2.setText(val + "%");
        tv3.setText(val2 + "%");
    }

}
