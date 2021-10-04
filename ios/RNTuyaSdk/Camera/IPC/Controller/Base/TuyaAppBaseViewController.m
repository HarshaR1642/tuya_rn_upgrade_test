//
//  TYBaseViewController.m
//  TuyaSmart
//
//  Created by 冯晓 on 16/1/4.
//  Copyright © 2016年 Tuya. All rights reserved.
//

#import "TuyaAppBaseViewController.h"
#import "TuyaAppProgressUtils.h"
#import "UIViewController+TuyaAppCategory.h"
#import "TuyaAppTheme.h"

@interface TuyaAppBaseViewController()<UIGestureRecognizerDelegate>

@property (nonatomic,assign) BOOL            loadAtFirstTime;
@property (nonatomic,strong) NSDictionary    *query;

@end

@implementation TuyaAppBaseViewController

@synthesize topBarView           = _topBarView;
@synthesize leftBackItem         = _leftBackItem;
@synthesize centerTitleItem      = _centerTitleItem;
@synthesize centerLogoItem       = _centerLogoItem;


- (void)dealloc {
    [self cancelService];
}

- (void)cancelService {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (instancetype)initWithQuery:(NSDictionary *)query {
    if (self = [super init]) {
    }
    return self;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    
    if (!_loadAtFirstTime) {
        _loadAtFirstTime = YES;
        [self viewDidAppearAtFirstTime:animated];
        return;
    }
    
    [self viewDidAppearNotAtFirstTime:animated];
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
}

- (void)viewDidAppearNotAtFirstTime:(BOOL)animated {}

- (void)viewDidAppearAtFirstTime:(BOOL)animated {}

- (void)viewDidLoad {
    
//    self.view.backgroundColor = HEXCOLOR(0xE8E9EF);
    
    [self.navigationController.navigationBar setHidden:YES];
    
    self.navigationController.interactivePopGestureRecognizer.delegate = self;
    self.navigationController.interactivePopGestureRecognizer.enabled = YES;
    
    self.automaticallyAdjustsScrollViewInsets = NO;
    
    [super viewDidLoad];
    
    [self initTopBarView];
    
    
    if ([self titleForEmptyView].length > 0) {
        [self.view addSubview:self.emptyView];
    }
}


- (void)showEmptyView {
    [self.view bringSubviewToFront:self.emptyView];
    self.emptyView.hidden = NO;
}

- (void)hideEmptyView {
    self.emptyView.hidden = YES;
}

- (void)initTopBarView {
    
    NSString *centerTitle = [self titleForCenterItem];
    NSString *rightTitle  = [self titleForRightItem];
    
    UIView *centerView = [self customViewForCenterItem];
    UIView *rightView = [self customViewForRightItem];
    
    NSInteger num = self.navigationController.viewControllers.count;
    if ([self tp_isModal] && num <= 1) {
        
        self.topBarView.leftItem = self.leftCancelItem;
        
    } else {
        
        if (num > 1) {
//            self.topBarView.leftItem = self.leftBackItem;
        }
    }
    
    if (centerTitle.length > 0) {
        UIFont *font = [UIFont fontWithName:@"Quicksand-Bold" size:17.0];
        [self.centerTitleItem setTitleTextAttributes:@{NSFontAttributeName: font} forState:UIControlStateNormal];
        self.centerTitleItem.title = centerTitle;
        self.topBarView.centerItem = self.centerTitleItem;
        [self.centerTitleItem setTitleTextAttributes:@{NSFontAttributeName: font} forState:UIControlStateNormal];
        
    } else if (centerView) {
        
        self.centerTitleItem.customView = centerView;
        self.topBarView.centerItem = self.centerTitleItem;
        
    }
    
    if (rightTitle.length > 0) {
        
        self.rightTitleItem.title = rightTitle;
        self.topBarView.rightItem = self.rightTitleItem;
        
    } else if (rightView) {
        
        self.rightTitleItem.customView = rightView;
        self.topBarView.rightItem = self.rightTitleItem;
        
    }
    self.topBarView.backgroundColor = [UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0];    
    [self.view addSubview:self.topBarView];
}

//重载方法
- (NSString *)titleForCenterItem {
    return @"";
}

//重载方法
- (NSString *)titleForRightItem {
    return @"";
}

- (UIView *)customViewForCenterItem {
    return nil;
}

- (UIView *)customViewForRightItem {
    return nil;
}

- (void)showProgressView {
    [TuyaAppProgressUtils showMessag:nil toView:nil];
}

- (void)showProgressView:(NSString *)message {
    [TuyaAppProgressUtils showMessag:message toView:nil];
}

- (void)hideProgressView {
    [TuyaAppProgressUtils hideHUDForView:nil animated:NO];
}

- (TuyaAppTopBarView *)topBarView {
    if (!_topBarView) {
        _topBarView = [TuyaAppTopBarView new];
        _topBarView.bottomLineHidden = YES;
        _topBarView.layer.shadowColor = [UIColor clearColor].CGColor;
        _topBarView.layer.shadowOffset = CGSizeMake(0,1);
        _topBarView.layer.shadowOpacity = 1;
        _topBarView.layer.shadowRadius = 1;
        
        
    }
    return _topBarView;
}

- (TuyaAppBarButtonItem *)rightTitleItem {
    if (!_rightTitleItem) {
        _rightTitleItem = [TuyaAppBarButtonItem titleItem:@"" target:self action:@selector(rightBtnAction)];
        
    }
    return _rightTitleItem;
}

- (TuyaAppBarButtonItem *)leftBackItem {
    if (!_leftBackItem) {
        _leftBackItem = [TuyaAppBarButtonItem  leftItemImage:[TuyaAppViewUtil getImageFromBundleWithName:@"back_arraow"] backItemButton:self action:@selector(backButtonTap)];
    }
    return _leftBackItem;
}


- (TuyaAppBarButtonItem *)leftCancelItem {
    if (!_leftBackItem) {
        _leftBackItem = [TuyaAppBarButtonItem cancelItem:self action:@selector(CancelButtonTap)];
    }
    return _leftBackItem;
}


- (TuyaAppBarButtonItem *)rightCancelItem {
    if (!_rightCancelItem) {
        _rightCancelItem = [TuyaAppBarButtonItem cancelItem:self action:@selector(rightBtnAction)];
    }
    return _rightCancelItem;
}

- (TuyaAppBarButtonItem *)centerTitleItem {
    if (!_centerTitleItem) {
        _centerTitleItem = [TuyaAppBarButtonItem titleItem:@"" target:nil action:nil];
    }
    return _centerTitleItem;
}

- (TuyaAppBarButtonItem *)centerLogoItem {
    if (!_centerLogoItem) {
        _centerLogoItem = [TuyaAppBarButtonItem logoItem:[UIImage imageNamed:@"logo"] terget:nil action:nil];
    }
    return _centerLogoItem;
}

- (TuyaAppLoadingView *)loadingView {
    if (!_loadingView) {
        _loadingView = [[TuyaAppLoadingView alloc] initWithFrame:CGRectZero];
    }
    return _loadingView;
}

- (void)showLoadingView {
    [self.view addSubview:self.loadingView];
    self.loadingView.centerX = self.view.centerX;
    self.loadingView.centerY = self.view.centerY;
}


- (void)hideLoadingView {
    if (_loadingView) {
        [_loadingView removeFromSuperview];
        _loadingView = nil;
    }
}

- (TuyaAppEmptyView *)emptyView {
    if (!_emptyView) {
        NSString *title = [self titleForEmptyView];
        NSString *subTitle = [self subTitleForEmptyView];
        NSString *imageName = [self imageNameForEmptyView];
        
        CGRect emptyViewFrame = CGRectMake(0, APP_TOP_BAR_HEIGHT, APP_SCREEN_WIDTH, APP_VISIBLE_HEIGHT);
        if (title.length > 0 && subTitle.length > 0) {
            
            _emptyView = [[TuyaAppEmptyView alloc] initWithFrame:emptyViewFrame title:title subTitle:subTitle];
        } else {
            _emptyView = [[TuyaAppEmptyView alloc] initWithFrame:emptyViewFrame title:title imageName:imageName];
        }
        
        _emptyView.hidden = YES;
    }
    return _emptyView;
}

- (NSString *)titleForEmptyView {
    return @"";
}

- (NSString *)subTitleForEmptyView {
    return @"";
}

- (NSString *)imageNameForEmptyView {
    return @"ty_list_empty";
}

- (void)backButtonTap {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)CancelButtonTap {
    [self dismissViewControllerAnimated:YES completion:NULL];
}

- (void)rightBtnAction {
    
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (void)setAddLeftBarBackButtonEnabled:(BOOL)addLeftBarBackButtonEnabled {
    //This is for add back button and it should be called from viewWillAppear
    if (addLeftBarBackButtonEnabled) {
        UIButton *btnBack = [UIButton buttonWithType:UIButtonTypeCustom];
        [btnBack setFrame:CGRectMake(0, 0, 30, 30)];
        NSURL *rtfUrl = [[NSBundle mainBundle] URLForResource:@"Resources" withExtension:@"bundle"];
        NSBundle *imageBundle = [NSBundle bundleWithURL:rtfUrl];
        if (@available(iOS 13.0, *)) {
            [btnBack setImage:[[UIImage imageNamed:@"back_arraow" inBundle:imageBundle compatibleWithTraitCollection:nil] imageWithTintColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        } else {
            [btnBack setImage:[UIImage imageNamed:@"back_arraow" inBundle:imageBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        }
        [btnBack addTarget:self action:@selector(actionLeftBarButton:) forControlEvents:UIControlEventTouchUpInside];
        UIBarButtonItem *barButton =[[UIBarButtonItem alloc] initWithCustomView:btnBack];
        self.navigationItem.leftBarButtonItem = barButton;
    } else {
        [self.navigationItem setHidesBackButton:YES];
    }
}

- (void)actionLeftBarButton: (id)obj {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)setRightBarButtonWithImage:(NSString *) image {
    //This is for add back button and it should be called from viewWillAppear
        UIButton *btnBack = [UIButton buttonWithType:UIButtonTypeCustom];
        [btnBack setFrame:CGRectMake(0, 0, 30, 30)];
        [btnBack setImage:[TuyaAppViewUtil getImageFromBundleWithname:image forTintColor:[UIColor whiteColor]] forState:UIControlStateNormal];
        [btnBack addTarget:self action:@selector(actionRightBarButton:) forControlEvents:UIControlEventTouchUpInside];
        UIBarButtonItem *barButton =[[UIBarButtonItem alloc] initWithCustomView:btnBack];
        self.navigationItem.rightBarButtonItem = barButton;
}

- (void)actionRightBarButton: (id)obj {
    
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

#pragma mark - StatusBar

- (void)setStatusBarHidden:(BOOL)statusBarHidden {
    _statusBarHidden = statusBarHidden;
    [self setNeedsStatusBarAppearanceUpdate];
}

- (BOOL)prefersStatusBarHidden {
    return self.statusBarHidden;
}

@end
