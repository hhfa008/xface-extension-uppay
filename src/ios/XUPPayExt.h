
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

#import <Cordova/CDVPlugin.h>
#import "UPPayPlugin.h"
#import "UPBalanceQueryPlugin.h"

@class XUPPayExt;

/**
  用于处理支付结果信息，并回传给js页面
 */
@interface XUPPayDelegate : NSObject<UPPayPluginDelegate, UPBalanceQueryDelegate>
{
    NSString* _callbackId;
}

@property (nonatomic, unsafe_unretained) XUPPayExt *uppayExt;
@property(nonatomic) BOOL statusBarHidden;

- (id)initWithCallbackId:(NSString *)callbackId;

@end

/**
  UPPay扩展，用于使用银联支付控件进行支付操作
 */
@interface XUPPayExt : CDVPlugin
{
    // 该成员变量主要用于记录所有的UPPayDelegate对象，以防止delegate的回调还没有
    // 被调用之前，delegate被系统垃圾回收
    NSMutableArray *uppayDelegates;
}

/**
  启动支付控件进行支付操作
  @param arguments
    - 0 NSString *transSerialNumber 交易流水号信息，银联后台生成，通过商户后台返回到客户端并传入支付控件
    - 1 NSString *mode "00"：代表接入生产环境（正式版本需要），"01"：代表接入开发测试环境（测试版本需要）
    - 2 NSString *sysProvide 保留使用
    - 3 NSString *spId 保留使用
 */
- (void)startPay:(CDVInvokedUrlCommand*)command;

/**
  启动余额查询控件
  @param arguments
    - 0 NSString *pan 银行卡号
    - 1 NSString *mode "00"：代表接入生产环境（正式版本需要），"01"：代表接入开发测试环境（测试版本需要）
 */
- (void)startBalanceEnquire:(CDVInvokedUrlCommand*)command;

/**
  从注册的delegate集合中删除指定的对象
 */
- (void) removeUPPayDelegate:(XUPPayDelegate *)delegate;

@end
