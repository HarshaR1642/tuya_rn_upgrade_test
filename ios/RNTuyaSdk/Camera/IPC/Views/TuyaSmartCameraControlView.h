//
//  TuyaSmartCameraControlView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>

@class TuyaSmartCameraControlView;

@protocol TuyaSmartCameraControlViewDelegate <NSObject>

- (void)controlView:(TuyaSmartCameraControlView *)controlView didSelectedControl:(NSString *)identifier;

@end

@interface TuyaSmartCameraControlView : UIView

@property (nonatomic, strong) NSArray *sourceData;

@property (nonatomic, weak) id<TuyaSmartCameraControlViewDelegate> delegate;

- (void)enableControl:(NSString *)identifier;

- (void)disableControl:(NSString *)identifier;

- (void)selectedControl:(NSString *)identifier;

- (void)deselectedControl:(NSString *)identifier;

- (void)enableAllControl;

- (void)disableAllControl;

@end

