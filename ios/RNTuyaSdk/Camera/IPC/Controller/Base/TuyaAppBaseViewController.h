//
//  TYBaseViewController.h
//  TuyaSmart
//
//  Created by 冯晓 on 16/1/4.
//  Copyright © 2016年 Tuya. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TuyaAppLoadingView.h"
#import "TuyaAppTopBarView.h"
#import "TuyaAppEmptyView.h"

@interface TuyaAppBaseViewController : UIViewController

@property (nonatomic, strong) TuyaAppTopBarView    *topBarView;

@property (nonatomic, strong) TuyaAppBarButtonItem *rightTitleItem;

@property (nonatomic, strong) TuyaAppBarButtonItem *leftBackItem;

@property (nonatomic, strong) TuyaAppBarButtonItem *leftCancelItem;

@property (nonatomic, strong) TuyaAppBarButtonItem *rightCancelItem;

@property (nonatomic, strong) TuyaAppBarButtonItem *centerTitleItem;

@property (nonatomic, strong) TuyaAppBarButtonItem *centerLogoItem;

@property (nonatomic, strong) TuyaAppLoadingView   *loadingView;

@property (nonatomic, assign) BOOL             statusBarHidden;
@property (nonatomic, strong) TuyaAppEmptyView    *emptyView;



- (instancetype)initWithQuery:(NSDictionary *)query;

- (void)viewDidAppearAtFirstTime:(BOOL)animated;

- (void)viewDidAppearNotAtFirstTime:(BOOL)animated;

- (void)backButtonTap;

- (void)rightBtnAction;

- (void)CancelButtonTap;

- (void)cancelService;

#pragma mark - 小菊花 加载中

- (void)showLoadingView;

- (void)hideLoadingView;

#pragma mark - 黑色的大菊花
- (void)showProgressView;

- (void)showProgressView:(NSString *)message;

- (void)hideProgressView;

#pragma mark - 导航栏

- (NSString *)titleForCenterItem;

- (NSString *)titleForRightItem;

- (UIView *)customViewForCenterItem;

- (UIView *)customViewForRightItem;

#pragma mark - 空页面
- (NSString *)titleForEmptyView;

- (NSString *)subTitleForEmptyView;

- (NSString *)imageNameForEmptyView;

- (void)showEmptyView;

- (void)hideEmptyView;


@end
