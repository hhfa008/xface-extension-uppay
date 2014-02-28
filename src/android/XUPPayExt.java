
/*
 Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
 This program is distributed under the terms of the GNU General Public License.

 This file is part of xFace.

 xFace is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 xFace is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with xFace.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.polyvi.xface.extension.uppay;

import java.util.Locale;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.polyvi.xface.util.XLog;
import com.unionpay.mobile.plugin.zl.UPLauncher;
import com.unionpay.mobile.uppay.v2.UPLauncherV2;

import android.content.Intent;


/**
 * XUPPayExt主要是封装启动银联支付控件的接口和接收交易结束后控件返回的交易结果<br/>
 * dependent-libs: UPPayPluginEx.jar,UPPluginWidget.jar,armeabi/libCDZL.so,armeabi/libCdzlWidget.so
 */
public class XUPPayExt extends CordovaPlugin {
    /**
     * 1.该扩展需要在AndroidManifest.xml配置<activity>标签
     * <activity android:name="com.unionpay.mobile.uppay.v2.MobileActivityEx"
     *      android:configChanges="orientation|keyboardHidden"
     *      android:screenOrientation="portrait"
     *      android:theme="@style/Transparent"
     *      android:windowSoftInputMode="adjustResize" />
     * <activity android:name="com.unionpay.mobile.plugin.zl.MobileActivity"
     *      android:configChanges="orientation|keyboardHidden"
     *      android:screenOrientation="portrait"
     *      android:theme="@style/Transparent"
     *      android:windowSoftInputMode="adjustResize" />
     * 2.该扩展需要在AndroidManifest.xml配置的权限有
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
     * <uses-permission android:name="android.permission.INTERNET" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     * 3.在res/values中增加style.xml文件
     */
    private static final String CLASS_NAME = XUPPayExt.class.getSimpleName();
    private static final String KEY_PAY_RESULT = "pay_result";
    private static final String PAY_RESULT_SUCCESS = "success";

    private static final String COMMAND_START_PAY = "startPay";
    private static final String COMMAND_START_BALANCE_ENQUIRE = "startBalanceEnquire";

    /**
     * 银联支付控件或者无卡余额查询控件启动时所使用的requestCode，与UPPayPluginEx.jar和UPPluginWidget.jar中的实际值一致
     */
    private static final int UPPAY_PLUGIN_REQUEST_CODE = 100;

    private CallbackContext mCallbackCtx;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        cordova.setActivityResultCallback(this);
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) throws JSONException {
        // 目前不支持同时进行多个支付操作
        mCallbackCtx = callbackContext;
        if (COMMAND_START_PAY.equals(action)) {
            String transSerialNumber = args.getString(0);
            // "00"：代表接入生产环境（正式版本需要）
            // "01"：代表接入开发测试环境（测试版本需要）
            // "99": 代表pm测试环境（测试版本需要）
            String mode = args.getString(1);
            JSONArray cards = null;
            if (!args.isNull(4)) {
                cards = args.getJSONArray(4);
            }
            // 保留域
            String reserved = null;
            UPLauncher.startUPPay(cordova.getActivity(),
                    transSerialNumber, cards, mode, reserved);
        } else if (COMMAND_START_BALANCE_ENQUIRE.equals(action)) {
            String pan = args.getString(0);
            // "00":银联生产环境
            // "01":银联测试环境
            String mode = args.getString(1);
            // 保留域
            String reserved = null;
            UPLauncherV2.startBalanceEnquire(cordova.getActivity(), pan,
                    mode, reserved);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String payResult = "fail";
        if (intent != null) {
            if (UPPAY_PLUGIN_REQUEST_CODE == requestCode) {
                payResult = intent.getExtras().getString(KEY_PAY_RESULT)
                        .toLowerCase(Locale.ENGLISH);
                if (null != payResult) {
                    if (payResult.equals(PAY_RESULT_SUCCESS)) {
                        mCallbackCtx.success(PAY_RESULT_SUCCESS);
                    } else {
                        mCallbackCtx.error(payResult);
                    }
                } else {
                    // 余额
                    String balanceAmount = intent.getExtras().getString(
                            "balance_amount");
                    // 可用余额
                    String balanceAvailableAmount = intent.getExtras().getString(
                            "balance_available_amount");
                    JSONObject jsonObj = null;
                    if (null == balanceAmount && null == balanceAvailableAmount) {
                        XLog.e(CLASS_NAME, "Return params: balanceAmount or balanceAvailableAmount is null");
                        mCallbackCtx.error(payResult);
                    } else {
                        jsonObj = new JSONObject();
                        try {
                            jsonObj.put("balance", balanceAmount);
                            jsonObj.put("availableBalance", balanceAvailableAmount);
                            mCallbackCtx.success(jsonObj);
                        } catch (JSONException e) {
                            XLog.e(CLASS_NAME, "onActivityResult put element into jsonObj error!");
                            e.printStackTrace();
                            mCallbackCtx.error(payResult);
                        }
                    }
                }
            }
        }
        mCallbackCtx.error(payResult);
    }
}
