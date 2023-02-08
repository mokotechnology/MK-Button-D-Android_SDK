package com.moko.bxp.button.d.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.button.d.R;
import com.moko.bxp.button.d.databinding.DActivityAccDataBinding;
import com.moko.bxp.button.d.dialog.BottomDialog;
import com.moko.bxp.button.d.dialog.LoadingMessageDialog;
import com.moko.bxp.button.d.utils.ToastUtils;
import com.moko.support.d.DMokoSupport;
import com.moko.support.d.OrderTaskAssembler;
import com.moko.support.d.entity.OrderCHAR;
import com.moko.support.d.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

public class AccDataActivity extends BaseActivity{

    private DActivityAccDataBinding mBind;
    private boolean mReceiverTag = false;
    private ArrayList<String> axisDataRates;
    private ArrayList<String> axisScales;
    private boolean isSync;
    private int mSelectedRate;
    private int mSelectedScale;
    public boolean isConfigError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = DActivityAccDataBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        axisDataRates = new ArrayList<>();
        axisDataRates.add("1Hz");
        axisDataRates.add("10Hz");
        axisDataRates.add("25Hz");
        axisDataRates.add("50Hz");
        axisDataRates.add("100Hz");
        axisScales = new ArrayList<>();
        axisScales.add("±2g");
        axisScales.add("±4g");
        axisScales.add("±8g");
        axisScales.add("±16g");

        EventBus.getDefault().register(this);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!DMokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            DMokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getAxisParams());
            DMokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAxisParams());
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
                    finish();
                }
                if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                    // 设备连接成功，通知页面更新
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
                            if (flag == 0x01 && length == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_AXIS_PARAMS:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(AccDataActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_AXIS_PARAMS:
                                        if (length == 4) {
                                            mSelectedRate = value[4] & 0xFF;
                                            mSelectedScale = value[5] & 0xFF;
                                            int threshold = MokoUtils.toInt(Arrays.copyOfRange(value, 6, 8));
                                            mBind.tvAxisDataRate.setText(axisDataRates.get(mSelectedRate));
                                            mBind.tvAxisScale.setText(axisScales.get(mSelectedScale));
                                            mBind.etMotionThreshold.setText(String.valueOf(threshold));
                                            if (mSelectedScale == 0) {
                                                mBind.tvMotionThresholdUnit.setText("x1mg");
                                            } else if (mSelectedScale == 1) {
                                                mBind.tvMotionThresholdUnit.setText("x2mg");
                                            } else if (mSelectedScale == 2) {
                                                mBind.tvMotionThresholdUnit.setText("x4mg");
                                            } else if (mSelectedScale == 3) {
                                                mBind.tvMotionThresholdUnit.setText("x12mg");
                                            }
                                        }
                                        break;

                                }
                            }
                        }
                        break;
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_ACC:
                        if (value.length > 9) {
                            mBind.tvXData.setText(String.format("X-axis:%dmg", MokoUtils.toIntSigned(Arrays.copyOfRange(value,4,6))));
                            mBind.tvYData.setText(String.format("Y-axis:%dmg", MokoUtils.toIntSigned(Arrays.copyOfRange(value,6,8))));
                            mBind.tvZData.setText(String.format("Z-axis:%dmg", MokoUtils.toIntSigned(Arrays.copyOfRange(value,8,10))));
                        }
                        break;
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
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

    private void back() {
        // 关闭通知
        DMokoSupport.getInstance().disableAccNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onBack(View view) {
        back();
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        String thresholdStr = mBind.etMotionThreshold.getText().toString();
        if (TextUtils.isEmpty(thresholdStr)) {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
            return;
        }
        int threshold = Integer.parseInt(thresholdStr);
        if (threshold < 1 || threshold > 2048) {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
            return;
        }
        // 保存
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setAxisParams(mSelectedRate, mSelectedScale, threshold));
        DMokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onSync(View view) {
        if (isWindowLocked())
            return;
        if (!isSync) {
            isSync = true;
            DMokoSupport.getInstance().enableAccNotify();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            mBind.ivSync.startAnimation(animation);
            mBind.tvSync.setText("Stop");
        } else {
            DMokoSupport.getInstance().disableAccNotify();
            isSync = false;
            mBind.ivSync.clearAnimation();
            mBind.tvSync.setText("Sync");
        }
    }

    public void onAxisScale(View view) {
        if (isWindowLocked())
            return;
        BottomDialog scaleDialog = new BottomDialog();
        scaleDialog.setDatas(axisScales, mSelectedScale);
        scaleDialog.setListener(value -> {
            mSelectedScale = value;
            if (mSelectedScale == 0) {
                mBind.tvMotionThresholdUnit.setText("x1mg");
            } else if (mSelectedScale == 1) {
                mBind.tvMotionThresholdUnit.setText("x2mg");
            } else if (mSelectedScale == 2) {
                mBind.tvMotionThresholdUnit.setText("x4mg");
            } else if (mSelectedScale == 3) {
                mBind.tvMotionThresholdUnit.setText("x12mg");
            }
            mBind.tvAxisScale.setText(axisScales.get(value));
        });
        scaleDialog.show(getSupportFragmentManager());
    }

    public void onAxisDataRate(View view) {
        if (isWindowLocked())
            return;
        BottomDialog dataRateDialog = new BottomDialog();
        dataRateDialog.setDatas(axisDataRates, mSelectedRate);
        dataRateDialog.setListener(value -> {
            mSelectedRate = value;
            mBind.tvAxisDataRate.setText(axisDataRates.get(value));
        });
        dataRateDialog.show(getSupportFragmentManager());
    }
}
