package com.xtmotion.zephyrhxmbtforgooglefit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ConnectListenerImpl;
import zephyr.android.HxMBT.ConnectedEvent;
import zephyr.android.HxMBT.ZephyrPacketArgs;
import zephyr.android.HxMBT.ZephyrPacketEvent;
import zephyr.android.HxMBT.ZephyrPacketListener;
import zephyr.android.HxMBT.ZephyrProtocol;

public class HxmConnectedListener extends ConnectListenerImpl {
    public static final int HEART_RATE = 0x100;
    private static final String TAG = "HXM_LISTENER";

    private Handler mDataHandler;
    private int GP_MSG_ID = 0x20;
    private int GP_HANDLER_ID = 0x20;
    private int HR_SPD_DIST_PACKET = 0x26;

    private HRSpeedDistPacketInfo HRSpeedDistPacket = new HRSpeedDistPacketInfo();

    public HxmConnectedListener(Handler handler) {
        super(handler, null);
        mDataHandler = handler;
    }

    public void Connected(ConnectedEvent<BTClient> eventArgs) {
        //Creates a new ZephyrProtocol object and passes it the BTComms object
        ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
        //ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
        _protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
            public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
                ZephyrPacketArgs msg = eventArgs.getPacket();
                byte CRCFailStatus;
                byte RcvdBytes;


                CRCFailStatus = msg.getCRCStatus();
                RcvdBytes = msg.getNumRvcdBytes();
                if (HR_SPD_DIST_PACKET == msg.getMsgID()) {

                    byte[] DataArray = msg.getBytes();

                    Bundle data = new Bundle();
                    data.putFloat("HeartRate", HRSpeedDistPacket.GetHeartRate(DataArray));
                    data.putDouble("InstantSpeed", HRSpeedDistPacket.GetInstantSpeed(DataArray));
                    data.putDouble("Distance", HRSpeedDistPacket.GetDistance(DataArray));
                    data.putDouble("Distance", HRSpeedDistPacket.GetCadence(DataArray));
                    data.putByte("Strides", HRSpeedDistPacket.GetStrides(DataArray));
//                    byte[] ts = HRSpeedDistPacket.GetHeartBeatTS(DataArray);

                    Message sendMsg = mDataHandler.obtainMessage(HEART_RATE);
                    sendMsg.setData(data);
                    mDataHandler.sendMessage(sendMsg);
                }
            }
        });
    }
}