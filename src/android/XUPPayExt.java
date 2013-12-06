
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
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;

import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

/**
 * XUPPayExt主要是封装启动银联支付控件的接口和接收交易结束后控件返回的交易结果<br/>
 * dependent-libs: UPPayAssistEx.jar,UPPayPluginEx.jar,armeabi/libentryex.so
 */
public class XUPPayExt extends CordovaPlugin {
    /**
     * 1.该扩展需要在AndroidManifest.xml配置<activity>标签
     * <activity android:name="com.unionpay.uppay.PayActivity"
     *      android:theme="@style/Theme.UPPay"
     *      android:label="@string/app_name"
     *      android:screenOrientation="portrait"
     *      android:configChanges="orientation|keyboardHidden"
     *      android:excludeFromRecents="true" />
     * <activity android:name="com.unionpay.uppay.PayActivityEx"
     *      android:label="@string/app_name"
     *      android:screenOrientation="portrait"
     *      android:configChanges="orientation|keyboardHidden"
     *      android:excludeFromRecents="true"
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

    private static final String KEY_PAY_RESULT = "pay_result";
    private static final String PAY_RESULT_SUCCESS = "success";

    private static final String COMMAND_START_PAY = "startPay";

    private CallbackContext mCallbackCtx;

    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) throws JSONException {
        // 目前不支持同时进行多个支付操作
        if (COMMAND_START_PAY.equals(action)) {
            mCallbackCtx = callbackContext;
            String transSerialNumber = args.getString(0);
            // "00"为正式平台，"01"测试平台,"99"测试平台
            String mode = args.getString(1);
            String sysProvide = args.getString(2);
            String spId = args.getString(3);
            UPPayAssistEx.startPayByJAR(cordova.getActivity(),
                    PayActivity.class, spId, sysProvide, transSerialNumber,
                    mode);
        }
        PluginResult r = new PluginResult(Status.NO_RESULT);
        mCallbackCtx.sendPluginResult(r);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Status status = Status.ERROR;
        String payResult = "fail";
        if (intent != null) {
            payResult = intent.getExtras().getString(KEY_PAY_RESULT)
                    .toLowerCase(Locale.ENGLISH);
            if (payResult.equals(PAY_RESULT_SUCCESS)) {
                status = Status.OK;
            } else {
                status = Status.ERROR;
            }
        }
        PluginResult r = new PluginResult(status, payResult);
        mCallbackCtx.sendPluginResult(r);
    }
}
