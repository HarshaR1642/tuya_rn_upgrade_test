//
//  CameraSettingsViewController.h
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 16/02/21.
//

#import <UIKit/UIKit.h>
#import "TuyaAppBaseViewController.h"
#import <TuyaSmartCameraKit/TuyaSmartCameraKit.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
NS_ASSUME_NONNULL_BEGIN

@interface CameraSettingsViewController : TuyaAppBaseViewController

@property (nonatomic, strong) NSString *devId;

@property (nonatomic, strong) TuyaSmartCameraDPManager                  *dpManager;

@end

NS_ASSUME_NONNULL_END
