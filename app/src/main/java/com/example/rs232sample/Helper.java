package com.example.rs232sample;

import android.app.Activity;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import com.kongqw.serialportlibrary.Device;
//import com.kongqw.serialportlibrary.SerialPort;
//import com.kongqw.serialportlibrary.SerialPortFinder;
//import com.kongqw.serialportlibrary.SerialPortManager;
//import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
//import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;
import androidx.core.content.ContextCompat;

import top.keepempty.sph.library.SerialPortConfig;
import top.keepempty.sph.library.SerialPortFinder;
import top.keepempty.sph.library.SphCmdEntity;
import top.keepempty.sph.library.SphResultCallback;
import top.keepempty.sph.library.SerialPortHelper;

public class Helper extends Activity implements AdapterView.OnItemSelectedListener {


    private static final String TAG = "SerialPortHelper";

    private SerialPortHelper serialPortHelper;

    private TextView mShowReceiveTxt;

    private Button mSendBtn;
    private Button mOpenBtn;

    private EditText mSendDataEt;

    private Spinner mPathSpinner;
    private Spinner mBaudRateSpinner;
    private Spinner mDataSpinner;
    private Spinner mCheckSpinner;
    private Spinner mStopSpinner;

    private int baudRate = 9600;
    private int dataBits = 8;
    private char checkBits = 'N';
    private int stopBits = 1;
    private String path;

    private SerialPortFinder mSerialPortFinder;
    private String[] entryValues;
    private boolean isOpen;
    private StringBuilder receiveTxt = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port);

        init();
    }

    private void init() {
        mPathSpinner = findViewById(R.id.sph_path);
        mBaudRateSpinner = findViewById(R.id.sph_baudRate);
        mDataSpinner = findViewById(R.id.sph_data);
        mCheckSpinner = findViewById(R.id.sph_check);
        mStopSpinner = findViewById(R.id.sph_stop);
        mOpenBtn = findViewById(R.id.sph_openBtn);
        mSendBtn = findViewById(R.id.sph_sendBtn);
        mShowReceiveTxt = findViewById(R.id.sph_showReceiveTxt);
        mSendDataEt = findViewById(R.id.sph_sendDataEt);

        mBaudRateSpinner.setSelection(13);
        mDataSpinner.setSelection(3);
        mCheckSpinner.setSelection(0);
        mStopSpinner.setSelection(0);

        mPathSpinner.setOnItemSelectedListener(this);
        mBaudRateSpinner.setOnItemSelectedListener(this);
        mDataSpinner.setOnItemSelectedListener(this);
        mCheckSpinner.setOnItemSelectedListener(this);
        mStopSpinner.setOnItemSelectedListener(this);

        mSerialPortFinder = new SerialPortFinder();
        entryValues = mSerialPortFinder.getAllDevicesPath();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                entryValues);
        mPathSpinner.setAdapter(adapter);

        mSendBtn = findViewById(R.id.sph_sendBtn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendTxt = mSendDataEt.getText().toString().trim();
                if(TextUtils.isEmpty(sendTxt)){
                    Toast.makeText(Helper.this,"????????????????????????",Toast.LENGTH_LONG).show();
                    return;
                }
                if (sendTxt.length() % 2 == 1) {
                    Toast.makeText(Helper.this,"???????????????",Toast.LENGTH_LONG).show();
                    return;
                }
                serialPortHelper.addCommands(sendTxt);
            }
        });

        mOpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpen){
                    serialPortHelper.closeDevice();
                    isOpen = false;
                }else{
                    openSerialPort();
                }
                showState();
            }
        });
    }

    /**
     * ????????????
     */
    private void openSerialPort(){

        /**
         * ????????????
         */
        SerialPortConfig serialPortConfig = new SerialPortConfig();
        serialPortConfig.mode = 0;
        serialPortConfig.path = path;
        serialPortConfig.baudRate = baudRate;
        serialPortConfig.dataBits = dataBits;
        serialPortConfig.parity   = checkBits;
        serialPortConfig.stopBits = stopBits;


        // ???????????????
        serialPortHelper = new SerialPortHelper(16);
        // ??????????????????
        serialPortHelper.setConfigInfo(serialPortConfig);
        // ????????????
        isOpen = serialPortHelper.openDevice();
        if(!isOpen){
            Toast.makeText(this,"?????????????????????",Toast.LENGTH_LONG).show();
        }
        serialPortHelper.setSphResultCallback(new SphResultCallback() {
            @Override
            public void onSendData(SphCmdEntity sendCom) {
                Log.d(TAG, "???????????????" + sendCom.commandsHex);
            }

            @Override
            public void onReceiveData(SphCmdEntity data) {
                Log.d(TAG, "???????????????" + data.commandsHex);

                receiveTxt.append(data.commandsHex).append("\n");
                mShowReceiveTxt.setText(receiveTxt.toString());
                Integer x = Integer.parseInt(receiveTxt.toString(),16);
                ShowMessage(receiveTxt.toString());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "??????");
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sph_path:
                path = entryValues[position];
                break;
            case R.id.sph_baudRate:
                String[] baud_rates = getResources().getStringArray(R.array.baud_rate_arr);
                baudRate = Integer.parseInt(baud_rates[position]);
                break;
            case R.id.sph_data:
                String[] data_rates = getResources().getStringArray(R.array.data_bits_arr);
                dataBits = Integer.parseInt(data_rates[position]);
                break;
            case R.id.sph_check:
                String[] check_rates = getResources().getStringArray(R.array.check_digit_arr);
                checkBits = check_rates[position].charAt(0);
                break;
            case R.id.sph_stop:
                String[] stop_rates = getResources().getStringArray(R.array.stop_bits_arr);
                stopBits = Integer.parseInt(stop_rates[position]);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void showState(){
        if(isOpen){
            Toast.makeText(this,"?????????????????????",Toast.LENGTH_LONG).show();
            mOpenBtn.setText("????????????");
            mOpenBtn.setTextColor(ContextCompat.getColor(this,R.color.org));
            mOpenBtn.setBackgroundResource(R.drawable.button_style_stroke);
        }else {
            mOpenBtn.setText("????????????");
            mOpenBtn.setTextColor(ContextCompat.getColor(this,R.color.white));
            mOpenBtn.setBackgroundResource(R.drawable.button_style_org);
        }
    }


    public void clearSend(View view) {
        mSendDataEt.setText("");
    }

    public void clearReceive(View view) {
        receiveTxt = new StringBuilder();
        mShowReceiveTxt.setText("");
    }
    private void ShowMessage(String sMsg)
    {
        Toast.makeText(this, sMsg, Toast.LENGTH_SHORT).show();
    }
}