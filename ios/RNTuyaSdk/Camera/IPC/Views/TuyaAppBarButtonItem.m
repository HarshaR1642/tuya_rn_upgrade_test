//
//  TuyaAppBarButtonItem.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//
//

#import "TuyaAppBarButtonItem.h"
#import "TuyaAppBaseView.h"

@implementation TuyaAppBarButtonItem

// < 返回
+ (UIBarButtonItem *)backItem:(id)target action:(SEL)action {
    UIBarButtonItem *leftBackItem = [[UIBarButtonItem alloc] initWithTitle:UIKitLocalizedString(@"back") style:UIBarButtonItemStylePlain target:target action:action];
    leftBackItem.image = [UIImage imageNamed:@"tp_top_bar_back"];
    return leftBackItem;
}

// 取消
+ (UIBarButtonItem *)cancelItem:(id)target action:(SEL)action {
    UIButton *btnBack = [UIButton buttonWithType:UIButtonTypeCustom];
    [btnBack setFrame:CGRectMake(0, 0, 30, 30)];
    [btnBack setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"back_arraow"] forState:UIControlStateNormal];
    [btnBack addTarget:target action:action forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem *barButton = [[UIBarButtonItem alloc] initWithCustomView:btnBack];
    return barButton;
}

+ (UIBarButtonItem *)backItemImage:(UIImage *)image backItemButton:(id)target action:(SEL)action {
    UIButton *btnBack = [UIButton buttonWithType:UIButtonTypeCustom];
    [btnBack setFrame:CGRectMake(0, 0, 30, 30)];
    [btnBack setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"back_arraow"] forState:UIControlStateNormal];
    [btnBack addTarget:target action:action forControlEvents:UIControlEventTouchUpInside];
    UIBarButtonItem *barButton = [[UIBarButtonItem alloc] initWithCustomView:btnBack];
//    UIBarButtonItem *leftBackItem = [[UIBarButtonItem alloc] initWithImage:image style:UIBarButtonItemStylePlain target:target action:action];
    return barButton;
}


// 完成
+ (UIBarButtonItem *)doneItem:(id)target action:(SEL)action {
    return [[UIBarButtonItem alloc] initWithTitle:UIKitLocalizedString(@"Done") style:UIBarButtonItemStyleDone target:target action:action];
}

// 文字
+ (UIBarButtonItem *)titleItem:(NSString *)title target:(id)target action:(SEL)action {
    return [[UIBarButtonItem alloc] initWithTitle:title style:UIBarButtonItemStylePlain target:target action:action];
}

// 图片
+ (UIBarButtonItem *)logoItem:(UIImage *)image terget:(id)target action:(SEL)action {
    return [[UIBarButtonItem alloc] initWithImage:image style:UIBarButtonItemStylePlain target:target action:action];
}

// deprecated
// --------------------------------

+ (TuyaAppBarButtonItem *)rightTitleItem:(id)target action:(SEL)action {
    return [TuyaAppBarButtonItem titleItem:UIKitLocalizedString(@"Done") target:target action:action];
}

+ (TuyaAppBarButtonItem *)leftBackItem:(id)target action:(SEL)action {
    return [TuyaAppBarButtonItem backItem:target action:action];
}

+ (TuyaAppBarButtonItem *)leftCancelItem:(id)target action:(SEL)action {
    return [TuyaAppBarButtonItem cancelItem:target action:action];
}

+ (TuyaAppBarButtonItem *)rightCancelItem:(id)target action:(SEL)action {
    return [TuyaAppBarButtonItem cancelItem:target action:action];
}

+ (TuyaAppBarButtonItem *)centerTitleItem:(id)target action:(SEL)action {
    return [TuyaAppBarButtonItem titleItem:@"Title" target:target action:action];
}

+ (TuyaAppBarButtonItem *)centerLogoItem:(id)target action:(SEL)action {
    return [TuyaAppBarButtonItem logoItem:[UIImage imageNamed:@"logo"] terget:target action:action];
}

+ (TuyaAppBarButtonItem *)leftItemImage:(UIImage *)image backItemButton:(id)target action:(SEL)action {
    return [TuyaAppBarButtonItem backItemImage:image backItemButton:target action:action];
}

@end
