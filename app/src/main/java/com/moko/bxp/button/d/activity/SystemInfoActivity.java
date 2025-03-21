package com.moko.bxp.button.d.activity;

import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.button.d.AppConstants;
import com.moko.bxp.button.d.databinding.DActivitySystemInfoBinding;
import com.moko.bxp.button.d.dialog.LoadingMessageDialog;
import com.moko.support.d.DMokoSupport;
import com.moko.support.d.OrderTaskAssembler;
import com.moko.support.d.entity.OrderCHAR;
import com.moko.support.d.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class SystemInfoActivity extends BaseActivity {


    private DActivitySystemInfoBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = DActivitySystemInfoBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        int firmwareType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_DEVICE_TYPE, 0);
        mBind.rlBatteryPercent.setVisibility(firmwareType == 1 ? View.VISIBLE : View.GONE);
        EventBus.getDefault().register(this);
        if (!DMokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            DMokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getBattery());
            if (firmwareType == 1) {
                orderTasks.add(OrderTaskAssembler.getBatteryPercent());
            }
            orderTasks.add(OrderTaskAssembler.getDeviceMac());
            orderTasks.add(OrderTaskAssembler.getDeviceModel(firmwareType));
            orderTasks.add(OrderTaskAssembler.getSoftwareVersion(firmwareType));
            orderTasks.add(OrderTaskAssembler.getFirmwareVersion(firmwareType));
            orderTasks.add(OrderTaskAssembler.getHardwareVersion(firmwareType));
            orderTasks.add(OrderTaskAssembler.getProductDate(firmwareType));
            orderTasks.add(OrderTaskAssembler.getManufacturer(firmwareType));
            DMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    SystemInfoActivity.this.finish();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length > 4) {
                            int header = value[0] & 0xFF;// 0xEB
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            if (header != 0xEB)
                                return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[3] & 0xFF;
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_MANUFACTURER:
                                        if (length > 0) {
                                            String manufacturer = new String(Arrays.copyOfRange(value, 4, 4 + length));
                                            mBind.tvManufacturer.setText(manufacturer);
                                        }
                                        break;
                                    case KEY_FIRMWARE_REVISION:
                                        if (length > 0) {
                                            String firmwareVersion = new String(Arrays.copyOfRange(value, 4, 4 + length));
                                            mBind.tvFirmwareVersion.setText(firmwareVersion);
                                        }
                                        break;
                                    case KEY_SOFTWARE_REVISION:
                                        if (length > 0) {
                                            String softwareVersion = new String(Arrays.copyOfRange(value, 4, 4 + length));
                                            mBind.tvSoftwareVersion.setText(softwareVersion);
                                        }
                                        break;
                                    case KEY_HARDWARE_REVISION:
                                        if (length > 0) {
                                            String hardwareVersion = new String(Arrays.copyOfRange(value, 4, 4 + length));
                                            mBind.tvHardwareVersion.setText(hardwareVersion);
                                        }
                                        break;
                                    case KEY_MODEL_NUMBER:
                                        if (length > 0) {
                                            String model = new String(Arrays.copyOfRange(value, 4, 4 + length));
                                            mBind.tvDeviceModel.setText(model);
                                        }
                                        break;
                                    case KEY_PRODUCT_DATE:
                                        if (length > 0) {
                                            int year = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                            int month = value[6] & 0xFF;
                                            int day = value[7] & 0xFF;
                                            mBind.tvProductDate.setText(String.format(Locale.getDefault(), "%d/%02d/%02d", year, month, day));
                                        }
                                        break;
                                    case KEY_BATTERY_PERCENT:
                                        if (length == 1) {
                                            int percent = value[4] & 0xFF;
                                            mBind.tvBatteryPercent.setText(String.format(Locale.getDefault(), "%d%%", percent));
                                        }
                                        break;
                                    case KEY_BATTERY_VOLTAGE:
                                        if (length == 2) {
                                            int battery = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                            mBind.tvSoc.setText(String.format(Locale.getDefault(), "%dmV", battery));
                                        }
                                        break;
                                    case KEY_DEVICE_MAC:
                                        if (length == 6) {
                                            String mac = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 10));
                                            StringBuffer stringBuffer = new StringBuffer(mac);
                                            stringBuffer.insert(2, ":");
                                            stringBuffer.insert(5, ":");
                                            stringBuffer.insert(8, ":");
                                            stringBuffer.insert(11, ":");
                                            stringBuffer.insert(14, ":");
                                            mBind.tvMacAddress.setText(stringBuffer.toString().toUpperCase());
                                        }
                                        break;

                                }
                            }
                        }
                        break;
                    case CHAR_MODEL_NUMBER:
                        mBind.tvDeviceModel.setText(new String(value).trim());
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        mBind.tvSoftwareVersion.setText(new String(value).trim());
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        mBind.tvFirmwareVersion.setText(new String(value).trim());
                        break;
                    case CHAR_HARDWARE_REVISION:
                        mBind.tvHardwareVersion.setText(new String(value).trim());
                        break;
                    case CHAR_SERIAL_NUMBER:
                        mBind.tvProductDate.setText(new String(value).trim());
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        mBind.tvManufacturer.setText(new String(value).trim());
                        break;
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    public void onBack(View view) {
        finish();
    }
}
