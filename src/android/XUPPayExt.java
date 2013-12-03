
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.polyvi.xface.util.XNotification;
import com.polyvi.xface.util.XStrings;
import com.unionpay.UPPayAssistEx;

/**
 * XUPPayExt主要是封装启动银联支付控件的接口和接收交易结束后控件返回的交易结果<br/>
 * dependent-libs: UPPayAssistEx.jar,assets/UPPayPluginEx.apk
 */
public class XUPPayExt extends CordovaPlugin {

    private static final String KEY_PAY_RESULT = "pay_result";
    private static final String PAY_RESULT_SUCCESS = "success";
    private static String UPPAY_INSTALL_WARN = "uppay_install_warn";
    private static String PAY_PLUGIN_INSTALL_OPTIONS = "pay_plugin_install_options";

    private static final String COMMAND_START_PAY = "startPay";

    private CallbackContext mCallbackCtx;

    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) throws JSONException {
        Status status = Status.INVALID_ACTION;
        if (COMMAND_START_PAY.equals(action)) {
            mCallbackCtx = callbackContext;
            String transSerialNumber = args.getString(0);
            // "00"为正式平台，"01"测试平台,"99"测试平台
            String mode = args.getString(1);
            String sysProvide = args.getString(2);
            String spId = args.getString(3);

            int ret = UPPayAssistEx.startPay(cordova.getActivity(), spId,
                    sysProvide, transSerialNumber, mode);
            if (ret == UPPayAssistEx.PLUGIN_VALID) {
                status = Status.NO_RESULT;
                PluginResult r = new PluginResult(status);
                mCallbackCtx.sendPluginResult(r);
                return false;
            } else if (ret == UPPayAssistEx.PLUGIN_NOT_FOUND) {
                status = Status.ERROR;
                PluginResult r = new PluginResult(status,
                        "The uppay plugin apk is not installed!");
                showInstallationDialog();
                mCallbackCtx.sendPluginResult(r);
                return false;
            } else {
                status = Status.ERROR;
                PluginResult r = new PluginResult(status);
                mCallbackCtx.sendPluginResult(r);
                return false;
            }
        }
        PluginResult r = new PluginResult(status);
        mCallbackCtx.sendPluginResult(r);
        return true;
    }

    /**
     * 弹出一个确认对话框，提示用户安装银联支付控件插件
     */
    private void showInstallationDialog() {
        DialogInterface.OnClickListener clickedOk = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog instanceof AlertDialog) {
                    UPPayAssistEx.installUPPayPlugin(cordova.getActivity());
                }
            }
        };
        XNotification alert = new XNotification(cordova);
        alert.confirm(
                XStrings.getInstance().getString(UPPAY_INSTALL_WARN),
                "",
                XStrings.getInstance().getString(
                        PAY_PLUGIN_INSTALL_OPTIONS), clickedOk, null,
                null);
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
