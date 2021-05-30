//
//  TuyaAppTheme.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "TuyaAppTheme.h"
#import "TuyaAppTopBarView.h"
#import <WebKit/WebKit.h>
#import <YYModel/YYModel.h>

@implementation TuyaAppTheme

static TuyaAppTheme *_theme = nil;
+ (TuyaAppTheme *)theme {
    if (!_theme) {
        NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"RentlyCameraTheme" ofType:@"plist"];
        NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithContentsOfFile:plistPath];
        for (NSString *key in [dict.allKeys copy]) {
            NSString *value = [dict objectForKey:key];
            [dict setValue:[TuyaAppViewUtil colorWithHexString:value] forKey:key];
        }
        
        _theme = [self yy_modelWithJSON:dict];
        
        
        if (!_theme.home_index_bg_start_color) {
            //默认是主题色的75%
            _theme.home_index_bg_start_color = [_theme.navbar_font_color colorWithAlphaComponent:0.75];
        }
        
        if (!_theme.home_index_bg_end_color) {
            //默认是主题色的90%
            _theme.home_index_bg_end_color = [_theme.navbar_font_color colorWithAlphaComponent:0.9];
        }
        
    }
    return _theme;
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    UIColor *color = self.status_font_color;
    const CGFloat *components = CGColorGetComponents(color.CGColor);
    CGFloat light = components[0] * 0.3 + components[1] * 0.59 + components[2] * 0.11;
    if (light < 0.5) {
        return UIStatusBarStyleDefault;
    } else {
        return UIStatusBarStyleLightContent;
    }
}


- (UIColor *)disable_button_bg_color {
    return HEXCOLOR(0xC5C7CB);
}
@end

@implementation TuyaAppTheme (Appearance)

+ (void)load {
    [self initAppearance];
}

+ (void)initAppearance {
    
    [self initTPTopBarView];
    [self initNavigationBar];
    [self initTabBar];
    [self initWebView];
    
}

+ (void)initTPTopBarView {
    TuyaAppTopBarView *topBarView = [TuyaAppTopBarView appearance];
    topBarView.textColor = [TuyaAppTheme theme].status_font_color;
    topBarView.lineColor = [TuyaAppTheme theme].list_line_color;
    topBarView.backgroundColor = [TuyaAppTheme theme].status_bg_color;
    topBarView.topLineHidden = YES;
//    topBarView.bottomLineHidden = YES;
}

+ (void)initNavigationBar {
    UINavigationBar *navigationBar = [UINavigationBar appearance];
    navigationBar.titleTextAttributes = @{NSForegroundColorAttributeName: [TuyaAppTheme theme].status_font_color};
    navigationBar.tintColor = [TuyaAppTheme theme].status_font_color;
    navigationBar.barTintColor = [TuyaAppTheme theme].status_bg_color;
//    navigationBar.translucent = NO;
}

+ (void)initTabBar {
    UITabBar *tabBar = [UITabBar appearance];
    tabBar.tintColor = [TuyaAppTheme theme].navbar_font_color;
    tabBar.barTintColor = [UIColor whiteColor];
//    tabBar.translucent = NO;
}

+ (void)initWebView {    
    WKWebView *wkWebView = [WKWebView appearance];
    wkWebView.backgroundColor = [TuyaAppTheme theme].app_bg_color;
}

// http://stackoverflow.com/questions/17070582/using-uiappearance-and-switching-themes
+ (void)reloadAppearance {
    [self initAppearance];
    
    NSArray *windows = [UIApplication sharedApplication].windows;
    for (UIWindow *window in windows) {
        for (UIView *subView in window.subviews) {
            [subView removeFromSuperview];
            [window addSubview:subView];
        }
        [window.rootViewController setNeedsStatusBarAppearanceUpdate];
    }
}

+ (ZMJPreferences *)getToolTipGlobalPreference {
    static ZMJPreferences *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [ZMJPreferences new];
        sharedInstance.drawing.font = [UIFont fontWithName:@"Quicksand-Medium" size:16.0];
        sharedInstance.drawing.backgroundColor = [UIColor colorWithHue:.46 saturation:.99 brightness:.6 alpha:1];
        sharedInstance.drawing.foregroundColor = [UIColor darkGrayColor];
        sharedInstance.drawing.textAlignment = NSTextAlignmentCenter;
        sharedInstance.animating.dismissTransform = CGAffineTransformMakeTranslation(100, 0);
        sharedInstance.animating.showInitialTransform =CGAffineTransformMakeTranslation(-100, 0);
        sharedInstance.animating.showInitialAlpha = 0;
        sharedInstance.animating.showDuration = 1;
        sharedInstance.animating.dismissDuration = 1;
        ZMJTipView.globalPreferences = sharedInstance;
    });
    return sharedInstance;
}

@end

@implementation UIViewController(Appearance)

- (UIStatusBarStyle)preferredStatusBarStyle {
    return [TuyaAppTheme theme].preferredStatusBarStyle;
}

@end
