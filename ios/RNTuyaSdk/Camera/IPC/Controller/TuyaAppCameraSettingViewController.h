//
//  TuyaAppCameraSettingViewController.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>
#import "TuyaAppBaseViewController.h"
#import <TuyaSmartCameraKit/TuyaSmartCameraKit.h>

@interface TuyaAppCameraSettingViewController : TuyaAppBaseViewController

@property (nonatomic, strong) NSString *devId;

@property (nonatomic, strong) TuyaSmartCameraDPManager *dpManager;

@end
