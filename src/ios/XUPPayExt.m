
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

#import "XUPPayExt.h"
#import <Cordova/CDVInvokedUrlCommand.h>
#import <Cordova/CDVPluginResult.h>
#import <XFace/XConstants.h>

@implementation XUPPayDelegate

- (id) initWithCallbackId:(NSString *)callbackId
{
    self = [super init];
    if (self)
    {
        self->_callbackId = callbackId;
    }
    return self;
}

- (void)UPPayPluginResult:(NSString *)result
{
    /** result:
        支付成功：银行卡卡号
        支付失败：fail
        支付取消：cancel
     */
    CDVPluginResult *extResult;
    if([result isEqualToString:@"fail"] || [result isEqualToString:@"cancel"]){
        extResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:result];
    } else {
        extResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
    }
    [self.uppayExt.commandDelegate sendPluginResult:extResult callbackId:self->_callbackId];

    if(IsAtLeastiOSVersion(@"7.0"))
    {
        //在iOS 7.0上，还原status bar显示状态
        [[UIApplication sharedApplication] setStatusBarHidden:self.statusBarHidden];
    }

    [self.uppayExt removeUPPayDelegate:self];
    self.uppayExt = nil;
}

/**
    @成功调用控件以后，会执行回调函数。该函数返回一个字典，下面是关键字的意义：

    @key:result                value：(success,fail,cancel)
    @key:balance               value：账面余额（仅在成功时返回，其余为nil）
    @key:availableBalance      value：可用余额（仅在成功时返回，其余为nil）

 */
- (void)balanceQueryResult:(NSDictionary *)resultDict
{
    CDVPluginResult *extResult;
    NSString* result = [resultDict objectForKey:@"result"];
    if([result isEqualToString:@"success"])
    {
        extResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
    }
    else
    {
        extResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.uppayExt.commandDelegate sendPluginResult:extResult callbackId:self->_callbackId];

    if(IsAtLeastiOSVersion(@"7.0"))
    {
        //在iOS 7.0上，还原status bar显示状态
        [[UIApplication sharedApplication] setStatusBarHidden:self.statusBarHidden];
    }
    [self.uppayExt removeUPPayDelegate:self];
    self.uppayExt = nil;
}

@end

@implementation XUPPayExt

- (CDVPlugin*)initWithWebView:(UIWebView *)theWebView
{
    self = [super initWithWebView:theWebView];
    if (self)
    {
        self->uppayDelegates = [[NSMutableArray alloc] init];
    }

    return self;
}

- (void)startPay:(CDVInvokedUrlCommand *)command
{
    NSString *transSerialNumber = [command.arguments objectAtIndex:0];
    //"00" for formal environment, "01" for test environment
    NSString *mode = [command.arguments objectAtIndex:1];
    NSString *sysProvide = [command.arguments objectAtIndex:2];
    NSString *spId = [command.arguments objectAtIndex:3];
    NSArray *cards = [command argumentAtIndex:4 withDefault:[NSArray array]];
    XUPPayDelegate *delegate = [[XUPPayDelegate alloc] initWithCallbackId:command.callbackId];

    delegate.statusBarHidden = [UIApplication sharedApplication].statusBarHidden;
    if(IsAtLeastiOSVersion(@"7.0"))
    {
        // iOS 7.0上，使用全屏方式启动支付控件，解决状态栏与支付控件内容发生重叠的问题
        [[UIApplication sharedApplication] setStatusBarHidden:YES];
    }

    if ([UPPayPlugin respondsToSelector:@selector(startPay:sysProvide:spId:mode:viewController:delegate:)])
    {
        [UPPayPlugin startPay:transSerialNumber sysProvide:sysProvide spId:spId mode:mode viewController:[self viewController] delegate:delegate];
    }
    else if ([UPPayPlugin respondsToSelector:@selector(startPay:mode:viewController:delegate:)])
    {
        [UPPayPlugin startPay:transSerialNumber mode:mode viewController:[self viewController] delegate:delegate];
    }
    else if ([UPPayPlugin respondsToSelector:@selector(startPay:mode:viewController:delegate:cards:)])
    {
        [UPPayPlugin startPay:transSerialNumber mode:mode viewController:[self viewController] delegate:delegate cards:cards];
    }

    [uppayDelegates addObject:delegate];
    delegate.uppayExt = self;
}

- (void)startBalanceEnquire:(CDVInvokedUrlCommand *)command
{
    NSString *pan = [command.arguments objectAtIndex:0];
    //"00" for formal environment, "01" for test environment
    NSString *mode = [command.arguments objectAtIndex:1];
    XUPPayDelegate *delegate = [[XUPPayDelegate alloc] initWithCallbackId:command.callbackId];

    delegate.statusBarHidden = [UIApplication sharedApplication].statusBarHidden;
    if(IsAtLeastiOSVersion(@"7.0"))
    {
        // iOS 7.0上，使用全屏方式启动支付控件，解决状态栏与支付控件内容发生重叠的问题
        [[UIApplication sharedApplication] setStatusBarHidden:YES];
    }

    BOOL ret = [UPBalanceQueryPlugin startQueryBalanceWithPan:pan  mode:mode container:[self viewController] delegate:delegate];

    if (!ret) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        return;
    }

    [uppayDelegates addObject:delegate];
    delegate.uppayExt = self;
}

- (void) removeUPPayDelegate:(XUPPayDelegate *)delegate
{
    [uppayDelegates removeObject:delegate];
}

@end
