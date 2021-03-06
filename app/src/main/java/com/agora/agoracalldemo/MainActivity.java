/**
 * @file MainActivity.java
 * @brief This file implement the entry user interface of application
 *        User should select the application role (App or IoT device) firstly
 *        Then input account for login or register a new account
 * @author xiaohua.lu
 * @email luxiaohua@agora.io
 * @version 1.0.0.1
 * @date 2021-10-13
 * @license Copyright (C) 2021 AgoraIO Inc. All rights reserved.
 */
package com.agora.agoracalldemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import com.agora.agoracallkit.beans.UidInfoBean;
import com.agora.agoracallkit.callkit.AgoraCallKit;
import com.agora.agoracallkit.callkit.CallKitAccount;
import com.agora.agoracallkit.callkit.ICallKitCallback;
import com.hyphenate.easeim.R;
import com.hyphenate.easeim.databinding.ActivityMainBinding;

import java.util.ArrayList;


public class MainActivity extends BaseActivity implements ICallKitCallback {

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////// Constant Definition /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    private final String TAG = "DEMO/MainActivity";



    //
    // message Id
    //
    public static final int MSGID_ROLE_CHOICED = 0x1001;    ///< Application role already choiced


    ///////////////////////////////////////////////////////////////////////////
    ///////////////////// Variable Definition /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private static Handler mMsgHandler = null;      ///< ???????????????????????????

    private PushApplication mApplication;           ///< Current application
    private ActivityMainBinding mBinding;           ///< ???????????????view?????????
    private AppCompatEditText mAccountWgt;          ///< ??????????????????????????????
    private LoadingDialog mProgressDlg;             ///< ?????????????????????
    private LoadingDialog.Builder mDlgBuilder;      ///< ????????????????????????
    private int mChoicedRole = 0;                   ///< ???????????????, 1:??????device;  ??????:??????user
    private boolean mDblTalk = false;               ///< ????????????




    ///////////////////////////////////////////////////////////////////////////
    ///////////////////// Override Acitivyt Methods ////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "<onCreate>");
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mApplication = (PushApplication) getApplication();

        mMsgHandler = new Handler(this.getMainLooper())
        {
            @SuppressLint("HandlerLeak")
            public void handleMessage(Message msg)
            {
                switch (msg.what)
                {
                    case MSGID_ROLE_CHOICED:
                        onMsgRoleChoiced();
                        break;
                }
            }
        };

        initView();
    }


    @Override
    protected void onStart() {
        Log.d(TAG, "<onStart>");
        super.onStart();
    }

    @Override
    protected void onAllPermissionGranted()
    {
        popupRoleChoiceDlg();
    }


    @Override
    protected void onResume() {
        super.onResume();
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

        if (mMsgHandler != null) {  // remove all messages
            mMsgHandler.removeMessages(MSGID_ROLE_CHOICED);
            mMsgHandler = null;
        }

        //??????????????????
        AgoraCallKit.getInstance().unregisterListener(this);
    }

    /*
     * @brief ?????????????????????
     */
    private void initView() {
        // ?????????????????????
        mDlgBuilder = new LoadingDialog.Builder();
        mDlgBuilder.setMessage("");
        mDlgBuilder.setCancelable(false);
        mProgressDlg = mDlgBuilder.create(this);
        mProgressDlg.dismiss();

        //???view??????????????????????????????
        mAccountWgt = mBinding.etAccount;

        // ????????????????????????
        mBinding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnRegister();
            }
        });

        // ????????????????????????
        mBinding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnLogin();
            }
        });

    }




    /*
     * @brief ??????????????????
     */
    void onBtnRegister()
    {
        //?????????????????????
        mDlgBuilder.updateMessage("?????????...");
        mProgressDlg.show();

        //????????????
        int roleType = ((PushApplication) getApplication()).getRoleType();
        String accountName = mAccountWgt.getText().toString();
        CallKitAccount registerAccount = new CallKitAccount(accountName, roleType);

        int ret = AgoraCallKit.getInstance().accountRegister(registerAccount);
        if (ret != AgoraCallKit.ERR_NONE) {
            mProgressDlg.dismiss();
            popupMessage("??????????????????, ?????????: " + ret);
            return;
        }
    }

    /*
     * @brief ??????????????????
     */
    void onBtnLogin()
    {
        //?????????????????????
        mDlgBuilder.updateMessage("?????????...");
        mProgressDlg.show();

        int roleType = ((PushApplication) getApplication()).getRoleType();
        String accountName = mAccountWgt.getText().toString();
        CallKitAccount logInAccount = new CallKitAccount(accountName, roleType);

        int ret = AgoraCallKit.getInstance().accountLogIn(logInAccount);
        if (ret != AgoraCallKit.ERR_NONE) {
            mProgressDlg.dismiss();
            popupMessage("????????????, ?????????: " + ret);
            return;
        }
    }

    void popupRoleChoiceDlg()
    {
//        LayoutInflater inflater = LayoutInflater.from(this);
//        final View initEntryView = inflater.inflate(R.layout.init_application, null);
//        final RadioButton btnAsUser = (RadioButton)initEntryView.findViewById(R.id.btn_as_user);
//        final RadioButton btnAsDev = (RadioButton)initEntryView.findViewById(R.id.btn_as_dev);
//        final CheckBox cbDblTalk = (CheckBox)initEntryView.findViewById(R.id.cb_doubletalk);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("???????????????: ")
//                .setIcon(android.R.drawable.ic_menu_add)
//                .setView(initEntryView)
//                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (btnAsDev.isChecked()) {
//                            mChoicedRole = 1;
//                        } else {
//                            mChoicedRole = 0;
//                        }
//
//                        boolean dblTalkChecked = cbDblTalk.isChecked();
//                        mDblTalk = dblTalkChecked;
//
//                        mDlgBuilder.updateMessage("???????????????...");
//                        mProgressDlg.show();
//                        mMsgHandler.removeMessages(MSGID_ROLE_CHOICED);
//                        mMsgHandler.sendEmptyMessage(MSGID_ROLE_CHOICED);
//                    }});
//        builder.show();


        mDblTalk = false;
        mChoicedRole = 0;

        mDlgBuilder.updateMessage("???????????????...");
        mProgressDlg.show();
        mMsgHandler.removeMessages(MSGID_ROLE_CHOICED);
        mMsgHandler.sendEmptyMessage(MSGID_ROLE_CHOICED);
    }

    /*
     * @brief: ????????????????????????
     */
    void onMsgRoleChoiced()
    {
        if (mChoicedRole == 1) {
            ((PushApplication) getApplication()).setRoleType(UidInfoBean.TYPE_MAP_DEVICE);
        } else {
            ((PushApplication) getApplication()).setRoleType(UidInfoBean.TYPE_MAP_USER);
        }
        ((PushApplication) getApplication()).setDblTalk(mDblTalk);

        // ?????????????????????????????????????????????????????????????????????????????????
        mApplication.initializeEngine();

        //??????????????????
        AgoraCallKit.getInstance().registerListener(this);


        //???????????????????????????????????????????????????????????????????????????
        mDlgBuilder.updateMessage("???????????????...");
        mProgressDlg.show();

        int ret = AgoraCallKit.getInstance().accountAutoLogin();
        if (ret != AgoraCallKit.ERR_NONE) {
            mProgressDlg.dismiss();
            popupMessage("??????????????????, ?????????: " + ret);
            return;
        }
    }


    void popupMessage(String message)
    {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }




    ////////////////////////////////////////////////////////////////////////////
    /////////////////// Override ICallKitCallback Methods //////////////////////
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void onPeerIncoming(CallKitAccount account, CallKitAccount peer_account, String attachMsg) {
        Log.d(TAG, "<onPeerIncoming>");
        //?????????????????????????????????
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //???????????????????????????????????????UID??????
                Intent activityIntent = new Intent(MainActivity.this, CalledActivity.class);
                activityIntent.putExtra("caller_name", peer_account.getName());
                activityIntent.putExtra("attach_msg", attachMsg);
                startActivity(activityIntent);
            }
        });
    }

    @Override
    public void onRegisterDone(CallKitAccount account, int errCode) {
        Log.d(TAG, "<onRegisterDone>");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDlg.dismiss();

                if (errCode != AgoraCallKit.ERR_NONE)   {
                    popupMessage("??????: " + account.getName() + " ????????????");

                } else  {
                    popupMessage("??????: " + account.getName() + " ??????????????????????????????...");
                    onBtnLogin();
                }
            }
        });
    }

    @Override
    public void onLogInDone(CallKitAccount account, int errCode) {
        Log.d(TAG, "<onLogInDone>");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDlg.dismiss();

                if (account == null) {
                    popupMessage("????????????????????????????????????");
                    return;
                }

                if (errCode != AgoraCallKit.ERR_NONE)   {
                    popupMessage("??????: " + account.getName() + " ????????????");

                } else  {
                    // ???????????????????????????
                    Intent activityIntent = new Intent(MainActivity.this, LoggedActivity.class);
                    startActivity(activityIntent);
                }
            }
        });
    }

}