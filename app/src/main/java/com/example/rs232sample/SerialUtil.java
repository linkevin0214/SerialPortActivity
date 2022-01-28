package com.example.rs232sample;

import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.File;

public class SerialUtil {
    private SerialPortManager mSerialPortManager = new SerialPortManager();
    private static SerialUtil instance = null;

    // 此处使用单例模式
    public static SerialUtil getInstance() {
        if (instance == null) {
            synchronized (SerialUtil.class) {
                if (instance == null) {
                    instance = new SerialUtil();
                    instance.mSerialPortManager.setOnSerialPortDataListener(instance.onSerialPortDataListener);
                    instance.mSerialPortManager.setOnOpenSerialPortListener(instance.onOpenSerialPortListener);
                }
            }
        }
        return instance;
    }


    private OnSerialPortDataListener onSerialPortDataListener = new OnSerialPortDataListener() {
        @Override
        public void onDataReceived(byte[] bytes) {
            System.out.println("收到了数据。");
        }

        @Override
        public void onDataSent(byte[] bytes) {
            System.out.println("发送了数据。");
        }
    };

    private OnOpenSerialPortListener onOpenSerialPortListener = new OnOpenSerialPortListener() {
        @Override
        public void onSuccess(File device) {
            System.out.println("链接" + device.getName() +"成功");
        }

        @Override
        public void onFail(File device, Status status) {
            System.out.println("链接" + device.getName() +"失败");
        }
    };


    /**
     * 链接串口
     * @return
     */
    public boolean Connect() {
        return Connect("ttysWK1");
    }

    /**
     * 链接串口
     * @param PortName 设备节点名称
     * @return
     */
    public boolean Connect(String PortName) {
        return mSerialPortManager.openSerialPort(new File("dev/" + PortName), 9600);
    }

    /**
     * 写入数据
     * @param val
     */
    public void write(String val) {
        mSerialPortManager.sendBytes(val.getBytes());
    }

    public void write(byte[] val) {
        mSerialPortManager.sendBytes(val);
    }

}