//
//  CameraPlayBackController.h
//
//  Created by Roshan Bisht on 11/02/21.
//

#import <UIKit/UIKit.h>
#import "TuyaAppBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN

@class TuyaSmartCamera;
@interface CameraPlayBackController : TuyaAppBaseViewController
@property (nonatomic, strong) TuyaSmartCamera       *camera;
@end

NS_ASSUME_NONNULL_END
