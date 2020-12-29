//
//  TuyaAppCameraPlaybackViewController.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>
#import "TuyaAppBaseViewController.h"

@class TuyaSmartCamera;
@interface TuyaAppCameraPlaybackViewController : TuyaAppBaseViewController

@property (nonatomic, strong) TuyaSmartCamera *camera;

@end
