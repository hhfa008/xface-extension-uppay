//
//  UPPayPluginDelegate.h
//  UPPayDemo
//
//  Created by wang li on 12-2-7.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol UPPayPluginDelegate <NSObject>

/* result 参数说明
 * reslut 为控件调用返回参数
 *        支付成功返回：银行卡卡号
 *        支付失败返回：fail
 *        支付取消返回：cancel
 *
 */


-(void)UPPayPluginResult:(NSString*)result;
@end
