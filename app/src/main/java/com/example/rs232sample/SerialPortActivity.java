package com.example.rs232sample;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;
import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPortManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SerialPortActivity extends Activity implements OnOpenSerialPortListener {

    private static final String TAG = SerialPortActivity.class.getSimpleName();
    public static final String DEVICE = "device";
    private SerialPortManager mSerialPortManager;
    String result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port);

        Device device = (Device) getIntent().getSerializableExtra(DEVICE);
        Log.i(TAG, "onCreate: device = " + device);
        if (null == device) {
            finish();
            return;
        }

        mSerialPortManager = new SerialPortManager();

        //result = "I000000S01N03000000000000000000000000000000000000099002201271245500000000000000000000000000000000000000000000000000123456789123456789000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        //result = "I";
        //result =".E.......01N...............................000000009900220127124550.................................................SN0001PN0001000001...........................................................................................................................................................................................................................................................................";
       result ="I      S01E03                             000000009900220127124550                                                 SN0001PN0001000001                                                                                                                                                                                                                                                                                         ";
//        result ="I"+"      "+"S"+"01"+"E"+"03"+"      "+"                   "+"    "+"000000009900"+"220127"+"124550"+"         "+"Z"+"    "+"               "+"        "+"            "+"123456789123456789"+" "+"            "+"        "+"        "+"            "+"  "+"            "+"            "+"  "+"      "+"  "+" "+"        "+"        "+"            "+"     "+"                                                  "+"      "+" "+" "+"   "+"     "+"
        // 打开串口
        byte[] sendContentBytes = new byte[0];
        try {
            sendContentBytes = result.getBytes("US-ASCII");
            //ShowMessage(new String(convertStringToHex(result).getBytes()));
            System.out.println(new String(convertStringToHex(result).getBytes()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(this)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        Log.i(TAG, "onDataReceived [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "onDataReceived [ String ]: " + new String(bytes));
                        final byte[] finalBytes = bytes;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String str = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                                ShowMessage(new String(bytes));
                            }
                        });
                    }

                    @Override
                    public void onDataSent(byte[] bytes) {
                        Log.i(TAG, "onDataSent [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "onDataSent [ String ]: " + new String(bytes));
                        final byte[] finalBytes = bytes;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShowMessage(String.format("发送\n%s", new String(finalBytes)));
                            }
                        });
                    }
                })
                .openSerialPort(device.getFile(), 115200);

        Log.i(TAG, "onCreate: openSerialPort = " + openSerialPort);
    }
    private static String convertStringToHex(String str) {

        byte[] getBytesFromString = str.getBytes(StandardCharsets.UTF_8);
        BigInteger bigInteger = new BigInteger(1, getBytesFromString);

        String convertedResult = String.format("%x", bigInteger);

        //System.out.println("Converted Hex from String: " + convertedResult);
        return convertedResult;
    }
    @Override
    protected void onDestroy() {
        if (null != mSerialPortManager) {
            mSerialPortManager.closeSerialPort();
            mSerialPortManager = null;
        }
        super.onDestroy();
    }

    /**
     * 串口打开成功
     *
     * @param device 串口
     */
    @Override
    public void onSuccess(File device) {
        Toast.makeText(getApplicationContext(), String.format("串口 [%s] 打开成功", device.getPath()), Toast.LENGTH_SHORT).show();
    }

    /**
     * 串口打开失败
     *
     * @param device 串口
     * @param status status
     */
    @Override
    public void onFail(File device, Status status) {
        switch (status) {
            case NO_READ_WRITE_PERMISSION:
                ShowMessage(device.getPath()+"没有读写权限");
                break;
            case OPEN_FAIL:
            default:
                ShowMessage(device.getPath()+"串口打开失败");
                break;
        }
    }

    /**
     * 显示提示框
     *
     * @param title   title
     * @param message message
     */
    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * 发送数据
     *
     * @param view view
     */
    public void onSend(View view) {
        EditText editTextSendContent = (EditText) findViewById(R.id.et_send_content);
        if (null == editTextSendContent) {
            return;
        }
        String sendContent = editTextSendContent.getText().toString().trim();
        if (TextUtils.isEmpty(sendContent)) {
            Log.i(TAG, "onSend: 发送内容为 null");

            return;
        }
        //ShowMessage(result);


        boolean sendBytes = mSerialPortManager.sendBytes(convertStringToHex(editTextSendContent.toString()).getBytes());
        Log.i(TAG, "onSend: sendBytes = " + sendBytes);
        ShowMessage(sendBytes ? "发送成功" : "发送失败");
    }

    private Toast mToast;

    /**
     * Toast
     *
     * @param content content
     */
    private void showToast(String content) {
        if (null == mToast) {
            mToast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);
        }
        mToast.setText(content);
        mToast.show();
    }
    private void ShowMessage(String sMsg)
    {
        Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
    }
}
