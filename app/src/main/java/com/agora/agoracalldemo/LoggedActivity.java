/**
 * @file LoggedActivity.java
 * @brief This file implement main operate user interface,
 *        This UI should be display after login successful
 * @author xiaohua.lu
 * @email luxiaohua@agora.io
 * @version 1.0.0.1
 * @date 2021-10-13
 * @license Copyright (C) 2021 AgoraIO Inc. All rights reserved.
 */

package com.agora.agoracalldemo;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agora.agoracalldemo.LoadingDialog;
import com.agora.agoracalldemo.PushApplication;
import com.agora.agoracallkit.beans.IotAlarm;
import com.agora.agoracallkit.beans.ListenerInfoBean;
import com.agora.agoracallkit.beans.UidInfoBean;
import com.agora.agoracallkit.callkit.AgoraCallKit;
import com.agora.agoracallkit.callkit.AgoraCallNotify;
import com.agora.agoracallkit.callkit.CallKitAccount;
import com.agora.agoracallkit.callkit.ICallKitCallback;
import com.agora.agoracallkit.utils.CloudRecordResult;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.databinding.ActivitySecondBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LoggedActivity extends AppCompatActivity implements ICallKitCallback {
    private final String TAG = "DEMO/LoggedActivity";

    private PushApplication mApplication;           ///< Current application
    private ActivitySecondBinding mBinding;         ///< view?????????
    private AppCompatTextView mLocalAccountWgt;     ///< ??????????????????????????????
    private AppCompatEditText mCallAccountWgt;      ///< ??????????????????????????????
    private LoadingDialog mProgressDlg;             ///< ???????????????
    private LoadingDialog.Builder mDlgBuilder;      ///< ??????????????????
    private RecyclerView mBindDevRecycleView;       ///< ???????????????????????????


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        mApplication = (PushApplication) getApplication();

        //??????view?????????????????????????????????????????????????????????
        mBinding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        //view?????????
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //??????????????????
        AgoraCallKit.getInstance().registerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ??????????????????????????????????????????
        int state = AgoraCallKit.getInstance().getState();
        if ((AgoraCallKit.STATE_INVALID == state) ||
             (AgoraCallKit.STATE_SDK_READY == state)) {
            mProgressDlg.dismiss();
            finish();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "<onStop>");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "<onDestory>");
        super.onDestroy();
        //??????????????????
        AgoraCallKit.getInstance().unregisterListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBtnLogout();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initView() {
        // ?????????????????????
        mDlgBuilder = new LoadingDialog.Builder();
        mDlgBuilder.setMessage("");
        mDlgBuilder.setCancelable(false);
        mProgressDlg = mDlgBuilder.create(this);
        mProgressDlg.dismiss();

        //???view??????????????????????????????account
        mLocalAccountWgt = mBinding.editTextLocalAccount;
        mCallAccountWgt = mBinding.editTextCallAccount;

        String localName = "";
        CallKitAccount localAccount = AgoraCallKit.getInstance().getLocalAccount();
        assert (localAccount != null);
        int roleType = ((PushApplication) getApplication()).getRoleType();
        if (UidInfoBean.TYPE_MAP_DEVICE == roleType) {
            localName = "??????????????????: " + localAccount.getName();
            mBinding.btnCallDev.setChecked(false);
            mBinding.btnCallUsr.setChecked(true);
        } else {
            localName = "??????????????????: " + localAccount.getName();
            mBinding.btnCallDev.setChecked(true);
            mBinding.btnCallUsr.setChecked(false);
        }
        mLocalAccountWgt.setText(localName);
        mCallAccountWgt.setHint("?????????????????????????????????: ");


        // ??????????????????
        mBinding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnLogout();
            }
        });

        // ??????????????????
        mBinding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnCall();
            }
        });



        // ???????????????
        mBinding.btnDevBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBindNewDev();
            }
        });

        // ????????????
        mBinding.btnDevUnbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnUnbind();
            }
        });


        // ????????????????????????
        mBinding.btnDevRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnDevListRefresh();
            }
        });

        // ??????????????????
        ArrayList<String> devList = null;
        mBinding.rvDevices.setAdapter(new DeviceListAdapter(devList));
        mBinding.rvDevices.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvDevices.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(),
                mBinding.rvDevices, new RecyclerViewClickListener() {

            @Override
            public void onClick(View view, int position) {
                // mPgDigLogShow("???????????????...");

            }
        }));


        // ????????????????????????????????????
        mBinding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(LoggedActivity.this, SettingActivity.class);
                startActivity(activityIntent);
            }
        });

        // ?????????????????? (??????????????????)
        if (mApplication.getRoleType() == UidInfoBean.TYPE_MAP_DEVICE) {
            mBinding.btnAlarmTrigger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBtnAlarmTrigger();
                }
            });
        } else {
            mBinding.btnAlarmTrigger.setVisibility(View.INVISIBLE);
        }

        // ?????????????????? (??????????????????)
        if (mApplication.getRoleType() == UidInfoBean.TYPE_MAP_USER) {
            mBinding.btnAlarmQuery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBtnAlarmQuery();
                }
            });
        } else {
            mBinding.btnAlarmQuery.setVisibility(View.INVISIBLE);
        }
    }



    ////////////////////////////////////////////////////////////////////////////
    /////////////////// Implement all Buttons Events ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    void onBtnBindNewDev() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View bindEntryView = inflater.inflate(R.layout.bind_devices, null);
        final EditText etDevice1 = (EditText)bindEntryView.findViewById(R.id.et_device1);
        final EditText etDevice2 = (EditText)bindEntryView.findViewById(R.id.et_device2);
        final EditText etDevice3 = (EditText)bindEntryView.findViewById(R.id.et_device3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????????????????ID: ")
                .setIcon(android.R.drawable.ic_menu_add)
                .setView(bindEntryView)
                .setNegativeButton("??????", null)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceId1 = etDevice1.getText().toString();
                        String deviceId2 = etDevice2.getText().toString();
                        String deviceId3 = etDevice3.getText().toString();

                        ArrayList<String> devList = new ArrayList<>();
                        if (deviceId1.length() > 0) {
                            devList.add(deviceId1);
                        }
                        if (deviceId2.length() > 0) {
                            devList.add(deviceId2);
                        }
                        if (deviceId3.length() > 0) {
                            devList.add(deviceId3);
                        }

                        int ret = AgoraCallKit.getInstance().bindDevice(devList);
                        if (ret != AgoraCallKit.ERR_NONE) {
                            popupMessage("Fail to bind device");
                        }
                    }});
        builder.show();
    }

    void onBtnUnbind() {
//        List<String> devList = new ArrayList<>();
//        devList.add("dev001");
//        devList.add("dev002");
//        devList.add("dev003");
//        int ret = AgoraCallKit.getInstance().unbindDevice(devList);
//        if (ret != AgoraCallKit.ERR_NONE) {
//            popupMessage("Fail to unbind device");
//        }

        LayoutInflater inflater = LayoutInflater.from(this);
        final View bindEntryView = inflater.inflate(R.layout.bind_devices, null);
        final EditText etDevice1 = (EditText)bindEntryView.findViewById(R.id.et_device1);
        final EditText etDevice2 = (EditText)bindEntryView.findViewById(R.id.et_device2);
        final EditText etDevice3 = (EditText)bindEntryView.findViewById(R.id.et_device3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????????????????ID: ")
                .setIcon(android.R.drawable.ic_menu_add)
                .setView(bindEntryView)
                .setNegativeButton("??????", null)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String deviceId1 = etDevice1.getText().toString();
                        String deviceId2 = etDevice2.getText().toString();
                        String deviceId3 = etDevice3.getText().toString();

                        ArrayList<String> devList = new ArrayList<>();
                        if (deviceId1.length() > 0) {
                            devList.add(deviceId1);
                        }
                        if (deviceId2.length() > 0) {
                            devList.add(deviceId2);
                        }
                        if (deviceId3.length() > 0) {
                            devList.add(deviceId3);
                        }

                        int ret = AgoraCallKit.getInstance().unbindDevice(devList);
                        if (ret != AgoraCallKit.ERR_NONE) {
                            popupMessage("Fail to unbind device");
                        }
                    }});
        builder.show();
    }

    void onBtnDevListRefresh() {
        if (mApplication.getRoleType() == UidInfoBean.TYPE_MAP_USER) { // ????????????????????????????????????????????????
            int ret = AgoraCallKit.getInstance().queryBindedDevList();
            if (ret != AgoraCallKit.ERR_NONE) {
                popupMessage("Fail to querying binded device");
            }

        } else { // ????????????????????????????????????????????????
            int ret = AgoraCallKit.getInstance().queryBindedUserList();
            if (ret != AgoraCallKit.ERR_NONE) {
                popupMessage("Fail to querying binded user");
            }
        }
    }

    /*
     * @brief ????????????
     */
    void onBtnLogout() {
        //??????loading?????????
        mDlgBuilder.updateMessage("?????????...");
        mProgressDlg.show();

        // ????????????
        int ret = AgoraCallKit.getInstance().accountLogOut();
        if (ret != AgoraCallKit.ERR_NONE) {
            mProgressDlg.dismiss();
            popupMessage("????????????, ?????????: " + ret);
            return;
        }
    }

    /*
     * @brief ?????????????????? ???????????????
     */
    void onBtnCall()
    {
        // ????????????????????????
         int roleType;
        if (mBinding.btnCallUsr.isChecked()) {
            roleType = CallKitAccount.ACCOUNT_TYPE_USER;
        } else {
            roleType = CallKitAccount.ACCOUNT_TYPE_DEV;
        }
        String dialName = mCallAccountWgt.getText().toString();
        CallKitAccount dialAccount = new CallKitAccount(dialName, roleType);
        List<CallKitAccount> dialList = new ArrayList<>();
        dialList.add(dialAccount);
        int ret = AgoraCallKit.getInstance().callDial(dialList, "I'm a Agorain!");
        if (ret != AgoraCallKit.ERR_NONE) {
            mProgressDlg.dismiss();
            popupMessage("????????????, ?????????: " + ret);
            return;
        }
    }


    /*
     * @brief ?????????????????????????????????????????????????????????
     */
    void onBtnAlarmTrigger() {
        final EditText inputEditText = new EditText(this);
        inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????????????????: ")
                .setIcon(android.R.drawable.ic_menu_add)
                .setView(inputEditText)
                .setNegativeButton("??????", null)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String inputMessage = inputEditText.getText().toString();
                        if (inputMessage.isEmpty()) {
                            return;
                        }

                        int ret = AgoraCallKit.getInstance().triggerAlarm(inputMessage);
                        if (ret != AgoraCallKit.ERR_NONE) {
                            mProgressDlg.dismiss();
                            popupMessage("?????????????????????ret=" + ret);
                            return;
                        }

                    }});
        builder.show();
    }

    /*
     * @brief ??????????????????
     *
     */
    void onBtnAlarmQuery() {
        /*
        final EditText inputEditText = new EditText(this);
        inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????deviceId: ")
                .setIcon(android.R.drawable.ic_menu_add)
                .setView(inputEditText)
                .setNegativeButton("??????", null)
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String inputDevId = inputEditText.getText().toString();

                        if (inputDevId.isEmpty()) {
                            List<AgoraCallKit.AlarmRecord> recordList = AgoraCallKit.getInstance()
                                    .queryAllAlarm();
                            popupMessageLongTime("?????????????????????: " + recordList.size() + " ?????????\n"
                                        + combinRecordList(recordList));

                        } else {
//                            List<AgoraCallKit.AlarmRecord> recordList = AgoraCallKit.getInstance()
//                                    .queryAlarmByDeviceId(inputDevId);
//                            popupMessageLongTime("??????: " + inputDevId + " , ?????????: " + recordList.size() + " ?????????\n"
//                                    + combinRecordList(recordList));

                            Intent intent = new Intent(LoggedActivity.this, VideoPlayActivity.class);
                            intent.putExtra(VideoPlayActivity.DEVICE_ID_KEY, inputDevId);
                            startActivity(intent);
                        }

                    }});
        builder.show();
        */

        AgoraCallKit.AlarmQueryParam queryParam = new AgoraCallKit.AlarmQueryParam();
        queryParam.mYear = 0;
        queryParam.mMonth = 0;
        queryParam.mDay = 0;
        queryParam.mType = 1;
        queryParam.mPageIndex = 1;
        queryParam.mPageSize = 50;

        int ret = AgoraCallKit.getInstance().queryAlarmsFromServer(queryParam);
        if (ret != AgoraCallKit.ERR_NONE) {
            mProgressDlg.dismiss();
            popupMessage("???????????????????????????ret=" + ret);
            return;
        }

    }



    /*
     * @brief
     */
    String combinRecordList(List<AgoraCallKit.AlarmRecord> recordList) {
        if ((recordList == null) || (recordList.size() <= 0)) {
            return "{ }";
        }

        int count = recordList.size();
        String combine = " { ";

        for (int i = 0; i < (count-1); i++) {
            AgoraCallKit.AlarmRecord record = recordList.get(i);
            String alarmInfo = "device: " + record.mDeviceId + ", message=" + record.mMessage;
            combine = combine + alarmInfo + "; ";
        }

        AgoraCallKit.AlarmRecord lastRecord = recordList.get(count-1);
        String lastInfo = "device: " + lastRecord.mDeviceId + ", message=" + lastRecord.mMessage;
        combine = combine + lastInfo + " } ";
        return combine;


    }


    /*
     * @brief ?????????????????????????????????????????????????????????????????????????????? true??????????????? false
     */
    boolean isRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
            if (appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcessInfo.processName == context.getApplicationInfo().processName) {
                return true;
            }
        }
        return false;
    }

    //?????????????????????????????????????????????????????????
    void setTopApp(Context context) {
        if (isRunningForeground(context)) {
            return;
        }
        //??????ActivityManager
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        //?????????????????????task(??????)
        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
            //?????????????????? task???????????????????????????
            if (taskInfo.topActivity.getPackageName() == context.getPackageName()) {
                activityManager.moveTaskToFront(taskInfo.id, 0);
                Log.i(TAG, "<setTopApp> set to front, id=" + taskInfo.id);
                break;
            }
        }
    }

    void popupMessage(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    void popupMessageLongTime(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    ////////////////////////////////////////////////////////////////////////////
    /////////////////// Override ICallKitCallback Methods //////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onPeerIncoming(CallKitAccount account, CallKitAccount peer_account, String attachMsg) {
        //?????????????????????????????????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTopApp(LoggedActivity.this);

                if (mApplication.getRoleType() == UidInfoBean.TYPE_MAP_DEVICE) { // ???????????????????????????
                    //????????????
                    Log.i(TAG, "<onPeerIncoming> Answer the call directly");
                    AgoraCallKit.getInstance().callAnswer();

                    // ?????????????????????
                    Intent activityIntent = new Intent(LoggedActivity.this, LivingActivity.class);
                    activityIntent.putExtra("caller_name", peer_account.getName());
                    activityIntent.putExtra("call_state", "?????????...");
                    activityIntent.putExtra("answer", true);
                    startActivity(activityIntent);

                } else {
                    // ??????????????????????????????????????????????????????
                    Intent activityIntent = new Intent(LoggedActivity.this, com.agora.agoracalldemo.CalledActivity.class);
                    activityIntent.putExtra("caller_name", peer_account.getName());
                    activityIntent.putExtra("attach_msg", attachMsg);
                    startActivity(activityIntent);
                }

            }
        });
    }

    @Override
    public void onLoginOtherDevice(CallKitAccount account) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupMessage("???????????????????????????,??????????????????!");
                finish();
            }
        });
    }

    @Override
    public void onLogOutDone(CallKitAccount account, int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDlg.dismiss();

                if (errCode != AgoraCallKit.ERR_NONE)   {
                    popupMessage("??????: " + account.getName() + " ????????????");

                } else  {
                    // ????????????????????????????????????????????????????????????
                    finish();
                }
            }
        });
    }

    @Override
    public void onDialDone(CallKitAccount account, List<CallKitAccount> dialAccountList, int errCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDlg.dismiss();
                CallKitAccount dialAccount = dialAccountList.get(0);

                if (errCode != AgoraCallKit.ERR_NONE)   {
                    popupMessage("??????: " + dialAccount.getName() + " ??????");

                } else  {
                    // ???????????????????????????
                    Intent activityIntent = new Intent(LoggedActivity.this, com.agora.agoracalldemo.LivingActivity.class);
                    activityIntent.putExtra("caller_name",dialAccount.getName());
                    activityIntent.putExtra("call_state", "??????????????????...");
                    startActivity(activityIntent);
                }
            }
        });
    }

    @Override
    public void onBindDeviceDone(CallKitAccount account, List<CallKitAccount> bindedDevList, int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errCode == AgoraCallKit.ERR_NONE) {
                    refreshBindList(bindedDevList);
                    popupMessage("????????????");
                } else {
                    popupMessage("????????????");
                }
            }
        });
    }

    @Override
    public void onUnbindDeviceDone(CallKitAccount account, List<CallKitAccount> bindedDevList, int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errCode == AgoraCallKit.ERR_NONE) {
                    refreshBindList(bindedDevList);
                    popupMessage("????????????");
                } else {
                    popupMessage("????????????");
                }
            }
        });
    }

    @Override
    public void onQueryBindDevListDone(CallKitAccount account,
                                       List<CallKitAccount> bindedDevList, int errCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshBindList(bindedDevList);

                if (bindedDevList == null || bindedDevList.size() <= 0) {
                    popupMessage("?????????????????????????????????????????????");
                } else {
                    String devInfo = "";
                    for (int i = 0; i < bindedDevList.size(); i++) {
                        CallKitAccount devAccount = bindedDevList.get(i);
                        devInfo = devInfo +  devAccount.toString() + "\n";
                    }
                    popupMessage("???????????????????????????:\n" + devInfo);
                }
            }
        });
    }

    @Override
    public void onQueryBindUserListDone(CallKitAccount account,
                                       List<CallKitAccount> bindedUserList, int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshBindList(bindedUserList);

                if (bindedUserList == null || bindedUserList.size() <= 0) {
                    popupMessage("?????????????????????????????????????????????");
                } else {
                    String devInfo = "";
                    for (int i = 0; i < bindedUserList.size(); i++) {
                        CallKitAccount devAccount = bindedUserList.get(i);
                        devInfo = devInfo +  devAccount.toString() + "\n";
                    }
                    popupMessage("???????????????????????????:\n" + devInfo);
                }
            }
        });
    }


    @Override
    public void onCloudRecordingStart(CallKitAccount account, int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupMessage("?????????????????????......");
            }
        });
    }

    @Override
    public void onCloudRecordingStop(CallKitAccount account, CloudRecordResult recordResult, int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String fileListText = "";
                for (CloudRecordResult.RecordFileInfo fileInfo : recordResult.mFileList) {
                    fileListText = fileListText + fileInfo.mFilePath + "\n";
                }
                fileListText = fileListText + "?????????" + recordResult.mUploadingStatus;
                popupMessageLongTime("?????????????????????????????????\n" + fileListText);
            }
        });
    }

    @Override
    public void onAlarmSendDone(CallKitAccount account, String alarmMsg, int errCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errCode == AgoraCallKit.ERR_NONE) {
                    popupMessage("??????????????????: " + alarmMsg + " ??????!");
                } else {
                    popupMessage("??????????????????: " + alarmMsg + " ??????!");
                }
            }
        });
    }

    @Override
    public void onAlarmReceived(CallKitAccount account, CallKitAccount peer_account,
                                long timestamp, String alarmMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupMessage("???????????????: " + peer_account.getName() + " ???????????????: " + alarmMsg);
            }
        });
    }

    @Override
    public void onAlarmQueried(CallKitAccount account, AgoraCallKit.AlarmQueryParam queryParam,
                               ArrayList<IotAlarm> alarmList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                popupMessage("?????????????????????: " + alarmList.size() + " ???");
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Innternal Methods /////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    void refreshBindList(List<CallKitAccount> bindAccountList) {
        List<String> devStatusList = new ArrayList<>();
        if (bindAccountList != null) {
            for (int i = 0; i < bindAccountList.size(); i++) {
                CallKitAccount account = bindAccountList.get(i);
                String onOffLine = account.getOnline() ? "  online" : "  offline";
                String status = account.getName() + onOffLine;
                devStatusList.add(status);
            }
        }
        mBinding.rvDevices.setAdapter(new DeviceListAdapter(devStatusList));
        mBinding.rvDevices.invalidate();
    }

    String combineText(List<String> text_list) {
        if (text_list == null) {
            return " { } ";
        }
        int count = text_list.size();
        String combine = " { ";

        for (int i = 0; i < (count-1); i++) {
            combine = combine + text_list.get(i) + ", ";
        }

        combine = combine + text_list.get(count-1) + " } ";
        return combine;
    }

}