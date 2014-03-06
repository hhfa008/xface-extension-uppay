//
//  UPBalanceQueryDelegate.h
//  UPBalanceQuery
//
//  Created by liwang on 13-12-4.
//  Copyright (c) 2013年 liwang. All rights reserved.
//

#import <Foundation/Foundation.h>


#define kResult               @"result"
#define kBalance              @"balance"
#define kAvailableBalance     @"availableBalance"

@protocol UPBalanceQueryDelegate <NSObject>


/**
 
 @成功调用控件以后，会执行回调函数。该函数返回一个字典，下面是关键字的意义：
 
 @key:result                value：(success,fail,cancel)
 @key:balance               value：账面余额（仅在成功时返回，其余为nil）
 @key:availableBalance      value：可用余额（仅在成功时返回，其余为nil）

 */


- (void)balanceQueryResult:(NSDictionary *)result;

@end
