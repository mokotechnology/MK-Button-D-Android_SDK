package com.moko.support.d.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.d.entity.OrderCHAR;
import com.moko.support.d.entity.ParamsKeyEnum;

import androidx.annotation.IntRange;


public class ParamsTask extends OrderTask {
    public byte[] data;

    public ParamsTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void getData(ParamsKeyEnum key) {
        createGetParamsData(key.getParamsKey());
    }

    public void setData(ParamsKeyEnum key) {
        createSetParamsData(key.getParamsKey());
    }


    private void createGetParamsData(int paramsKey) {
        response.responseValue = data = new byte[]{(byte) 0xEA, (byte) 0x00, (byte) paramsKey, (byte) 0x00};
    }

    private void createSetParamsData(int paramsKey) {
        response.responseValue = data = new byte[]{(byte) 0xEA, (byte) 0x01, (byte) paramsKey, (byte) 0x00};
    }

    public void setAxisParams(@IntRange(from = 0, to = 4) int rate,
                              @IntRange(from = 0, to = 3) int scale,
                              @IntRange(from = 1, to = 2048) int sensitivity) {
        byte[] paramsBytes = MokoUtils.toByteArray(sensitivity, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AXIS_PARAMS.getParamsKey(),
                (byte) 0x04,
                (byte) rate,
                (byte) scale,
                paramsBytes[0],
                paramsBytes[1],
        };

    }

    public void setBleConnectable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BLE_CONNECTABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };

    }

    public void setEffectiveClickInterval(@IntRange(from = 500, to = 1500) int interval) {
        byte[] paramsBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_EFFECTIVE_CLICK_INTERVAL.getParamsKey(),
                (byte) 0x02,
                paramsBytes[0],
                paramsBytes[1],
        };

    }

    public void setScanResponseEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SCAN_RESPONSE_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };

    }

    public void setChangePasswordDisconnectEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_CHANGE_PASSWORD_DISCONNECT_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };

    }


    public void setButtonResetEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BUTTON_RESET_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };

    }

    public void getFrameType(@IntRange(from = 0, to = 3) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_FRAME_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
    }

    public void setAlarmFrameType(@IntRange(from = 0, to = 3) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_FRAME_TYPE.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) 0x00
        };

    }

    public void setUidFrameType(@IntRange(from = 0, to = 3) int slot, String namespaceId, String instanceId) {
        byte[] namespaceIdBytes = MokoUtils.hex2bytes(namespaceId);
        byte[] instanceIdBytes = MokoUtils.hex2bytes(instanceId);
        response.responseValue = data = new byte[22];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_FRAME_TYPE.getParamsKey();
        data[3] = (byte) 18;
        data[4] = (byte) slot;
        data[5] = (byte) 0x01;
        for (int i = 0; i < 10; i++) {
            data[i + 6] = namespaceIdBytes[i];
        }
        for (int i = 0; i < 6; i++) {
            data[i + 16] = instanceIdBytes[i];
        }
    }

    public void setIBeaconFrameType(@IntRange(from = 0, to = 3) int slot, String uuid, int major, int minor) {
        byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
        byte[] majorBytes = MokoUtils.toByteArray(major, 2);
        byte[] minorBytes = MokoUtils.toByteArray(minor, 2);
        response.responseValue = data = new byte[26];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_FRAME_TYPE.getParamsKey();
        data[3] = (byte) 22;
        data[4] = (byte) slot;
        data[5] = (byte) 0x02;
        for (int i = 0; i < 16; i++) {
            data[i + 6] = uuidBytes[i];
        }
        for (int i = 0; i < 2; i++) {
            data[i + 22] = majorBytes[i];
        }
        for (int i = 0; i < 2; i++) {
            data[i + 24] = minorBytes[i];
        }
    }

    public void getSlotParams(@IntRange(from = 0, to = 3) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };

    }

    public void setSlotParams(@IntRange(from = 0, to = 3) int slot,
                              @IntRange(from = 0, to = 1) int enable,
                              @IntRange(from = -100, to = 0) int rssi,
                              @IntRange(from = 20, to = 10000) int interval,
                              @IntRange(from = -40, to = 4) int txPower) {
        byte[] paramsBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey(),
                (byte) 0x06,
                (byte) slot,
                (byte) enable,
                (byte) rssi,
                paramsBytes[0],
                paramsBytes[1],
                (byte) txPower
        };

    }

    public void getSlotTriggerParams(@IntRange(from = 0, to = 3) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };

    }

    public void setSlotTriggerParams(@IntRange(from = 0, to = 3) int slot,
                                     @IntRange(from = 0, to = 1) int enable,
                                     @IntRange(from = -100, to = 0) int rssi,
                                     @IntRange(from = 20, to = 10000) int interval,
                                     @IntRange(from = -40, to = 4) int txPower,
                                     @IntRange(from = 1, to = 65535) int triggerAdvInterval) {
        byte[] paramsBytes = MokoUtils.toByteArray(interval, 2);
        byte[] triggerAdvIntervalBytes = MokoUtils.toByteArray(triggerAdvInterval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x08,
                (byte) slot,
                (byte) enable,
                (byte) rssi,
                paramsBytes[0],
                paramsBytes[1],
                (byte) txPower,
                triggerAdvIntervalBytes[0],
                triggerAdvIntervalBytes[1]
        };

    }

    public void getSlotAdvBeforeTriggerEnable(@IntRange(from = 0, to = 3) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_BEFORE_TRIGGER_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };

    }

    public void setSlotAdvBeforeTriggerEnable(@IntRange(from = 0, to = 3) int slot,
                                              @IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_BEFORE_TRIGGER_ENABLE.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) enable
        };

    }

    public void getSlotTriggerAlarmNotifyType(@IntRange(from = 0, to = 4) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_ALARM_NOTIFY_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };

    }

    public void setSlotTriggerAlarmNotifyType(@IntRange(from = 0, to = 3) int slot,
                                              @IntRange(from = 0, to = 5) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_ALARM_NOTIFY_TYPE.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) type
        };

    }

    public void setAbnormalInactivityAlarmStaticInterval(@IntRange(from = 1, to = 65535) int time) {
        byte[] intervalBytes = MokoUtils.toByteArray(time, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ABNORMAL_INACTIVITY_ALARM_STATIC_INTERVAL.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1]
        };

    }

    public void setPowerSavingEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_POWER_SAVING_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };

    }

    public void setPowerSavingStaticTriggerTime(@IntRange(from = 1, to = 65535) int time) {
        byte[] intervalBytes = MokoUtils.toByteArray(time, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_POWER_SAVING_STATIC_TRIGGER_TIME.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1]
        };

    }

    public void getSlotLEDNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };

    }

    public void setSlotLEDNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                            @IntRange(from = 1, to = 6000) int time,
                                            @IntRange(from = 0, to = 100) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x05,
                (byte) slot,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };

    }

//    public void getSlotVibrationNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
//        response.responseValue = data = new byte[]{
//                (byte) 0xEA,
//                (byte) 0x00,
//                (byte) ParamsKeyEnum.KEY_SLOT_VIBRATION_NOTIFY_ALARM_PARAMS.getParamsKey(),
//                (byte) 0x01,
//                (byte) slot
//        };
//        
//    }
//
//    public void setSlotVibrationNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
//                                                  @IntRange(from = 1, to = 6000) int time,
//                                                  @IntRange(from = 100, to = 10000) int interval) {
//        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
//        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
//        response.responseValue = data = new byte[]{
//                (byte) 0xEA,
//                (byte) 0x01,
//                (byte) ParamsKeyEnum.KEY_SLOT_VIBRATION_NOTIFY_ALARM_PARAMS.getParamsKey(),
//                (byte) 0x05,
//                (byte) slot,
//                timeBytes[0],
//                timeBytes[1],
//                intervalBytes[0],
//                intervalBytes[1],
//        };
//        
//    }

    public void getSlotBuzzerNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };

    }

    public void setSlotBuzzerNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                               @IntRange(from = 1, to = 6000) int time,
                                               @IntRange(from = 0, to = 100) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x05,
                (byte) slot,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };

    }

    public void setRemoteLEDNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                              @IntRange(from = 0, to = 100) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REMOTE_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };

    }

    public void setRemoteBuzzerNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                 @IntRange(from = 0, to = 100) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REMOTE_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };

    }

    public void setDismissAlarmEnable(@IntRange(from = 0, to = 1) int enable) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_ALARM_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };

    }

    public void setDismissLEDNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                               @IntRange(from = 0, to = 100) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };

    }

    public void setDismissBuzzerNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                  @IntRange(from = 0, to = 100) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };

    }

    public void setDismissAlarmType(@IntRange(from = 0, to = 5) int type) {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_ALARM_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };

    }

    public void setDeviceId(String deviceId) {
        byte[] deviceIdBytes = MokoUtils.hex2bytes(deviceId);
        int length = deviceIdBytes.length;
        response.responseValue = data = new byte[4 + length];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_DEVICE_ID.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = deviceIdBytes[i];
        }

    }

    public void setDeviceName(String deviceName) {
        byte[] deviceNameBytes = deviceName.getBytes();
        int length = deviceNameBytes.length;
        response.responseValue = data = new byte[4 + length];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_DEVICE_NAME.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = deviceNameBytes[i];
        }
    }

    public void setResetBattery() {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_RESET_BATTERY.getParamsKey(),
                (byte) 0x00,
        };

    }
}
