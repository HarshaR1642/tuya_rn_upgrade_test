//
//  TuyaAppBarButtonItem.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>


@interface TuyaAppBarButtonItem : UIBarButtonItem

// < 返回
+ (TuyaAppBarButtonItem *)backItem:(id)target action:(SEL)action;

// 取消
+ (TuyaAppBarButtonItem *)cancelItem:(id)target action:(SEL)action;

// 完成
+ (TuyaAppBarButtonItem *)doneItem:(id)target action:(SEL)action;

// 文字
+ (TuyaAppBarButtonItem *)titleItem:(NSString *)title target:(id)target action:(SEL)action;

// 图片
+ (TuyaAppBarButtonItem *)logoItem:(UIImage *)image terget:(id)target action:(SEL)action;


// deprecated
// --------------------------------

+ (TuyaAppBarButtonItem *)rightTitleItem:(id)target action:(SEL)action;
+ (TuyaAppBarButtonItem *)leftBackItem:(id)target action:(SEL)action;
+ (TuyaAppBarButtonItem *)leftCancelItem:(id)target action:(SEL)action;
+ (TuyaAppBarButtonItem *)rightCancelItem:(id)target action:(SEL)action;
+ (TuyaAppBarButtonItem *)centerTitleItem:(id)target action:(SEL)action;
+ (TuyaAppBarButtonItem *)centerLogoItem:(id)target action:(SEL)action;




@end
