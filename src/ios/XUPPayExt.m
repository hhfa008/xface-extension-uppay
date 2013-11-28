
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

// result may be "success", "fail" or "cancel"
- (void)UPPayPluginResult:(NSString*)result
{
    CDVCommandStatus status = CDVCommandStatus_NO_RESULT;
    if([result isEqualToString:@"success"])
    {
        status = CDVCommandStatus_OK;
    }
    else
    {
        status = CDVCommandStatus_ERROR;
    }

    if(SYSTEM_VERSION_NOT_LOWER_THAN(@"7.0"))
    {
        //在iOS 7.0上，还原status bar显示状态
        [[UIApplication sharedApplication] setStatusBarHidden:self.statusBarHidden];
    }

    CDVPluginResult *extResult = [CDVPluginResult resultWithStatus:status messageAsString:result];
    [self.uppayExt.commandDelegate sendPluginResult:extResult callbackId:_callbackId];
    [self.uppayExt removeUPPayDelegate:self];
    self.uppayExt = nil;
}

@end

@implementation XUPPayExt

- (CDVPlugin*)initWithWebView:(UIWebView*)theWebView
{
    self = [super initWithWebView:theWebView];
    if (self)
    {
        self->uppayDelegates = [[NSMutableArray alloc] init];
    }

    return self;
}

- (void)startPay:(CDVInvokedUrlCommand*)command
{
    NSString *transSerialNumber = [command.arguments objectAtIndex:0];
    //"00" for formal environment, "01" for test environment
    NSString *mode = [command.arguments objectAtIndex:1];
    NSString *sysProvide = [command.arguments objectAtIndex:2];
    NSString *spId = [command.arguments objectAtIndex:3];
    XUPPayDelegate *delegate = [[XUPPayDelegate alloc] initWithCallbackId:command.callbackId];

    delegate.statusBarHidden = [UIApplication sharedApplication].statusBarHidden;
    if(SYSTEM_VERSION_NOT_LOWER_THAN(@"7.0"))
    {
        // iOS 7.0上，使用全屏方式启动支付控件，解决状态栏与支付控件内容发生重叠的问题
        [[UIApplication sharedApplication] setStatusBarHidden:YES];
    }

    [uppayDelegates addObject:delegate];
    delegate.uppayExt = self;

    if([UPPayPlugin respondsToSelector:@selector(startPay:sysProvide:spId:mode:viewController:delegate:)])
    {
        [UPPayPlugin startPay:transSerialNumber sysProvide:sysProvide spId:spId mode:mode viewController:[self viewController] delegate:delegate];
    }
    else
    {
        [UPPayPlugin startPay:transSerialNumber mode:mode viewController:[self viewController] delegate:delegate];
    }

}

- (void) removeUPPayDelegate:(XUPPayDelegate *)delegate
{
    [uppayDelegates removeObject:delegate];
}

@end
