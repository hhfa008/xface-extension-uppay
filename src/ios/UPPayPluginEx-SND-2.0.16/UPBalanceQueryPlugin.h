//
//  UPBalanceQueryPlugin.h
//  UPBalanceQueryPlugin
//
//  Created by liwang on 13-12-4.
//  Copyright (c) 2013年 liwang. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "UPBalanceQueryDelegate.h"

@interface UPBalanceQueryPlugin : NSObject


/**
 
 @参数错误将不能调用控件
 
 */

+ (BOOL)startQueryBalanceWithPan:(NSString *)pan
                            mode:(NSString *)mode
                       container:(UIViewController *)container
                        delegate:(id<UPBalanceQueryDelegate>)delegate;

@end
