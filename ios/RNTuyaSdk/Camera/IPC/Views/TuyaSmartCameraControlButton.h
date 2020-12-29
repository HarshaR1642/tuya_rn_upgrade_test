//
//  TuyaSmartCameraControlView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>

@interface TuyaSmartCameraControlButton : UIView

@property (nonatomic, strong) UIImageView *imageView;

@property (nonatomic, strong) UILabel *titleLabel;

@property (nonatomic, strong) NSString *identifier;

@property (nonatomic, assign) BOOL highLighted;

@property (nonatomic, assign) BOOL disabled;

- (void)addTarget:(id)target action:(SEL)action;

@end
