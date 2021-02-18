//
//  TuyaCameraModule.m
//  RNTuyaSdk
//
//  Created by Nagaraj Balan on 02/12/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import "TuyaCameraModule.h"
#import <TuyaSmartActivatorKit/TuyaSmartActivatorKit.h>
#import <TuyaSmartBaseKit/TuyaSmartBaseKit.h>
#import <TuyaSmartDeviceKit/TuyaSmartDeviceKit.h>
#import <TuyaSmartBaseKit/TuyaSmartBaseKit.h>
#import "TuyaRNUtils+Network.h"
#import "TuyaAppCameraViewController.h"
#import "CameraAppViewController.h"
#import "TuyaAppViewUtil.h"

#define kControlTalk        @"talk"
#define kControlRecord      @"record"
#define kControlPhoto       @"photo"
#define kControlPlayback    @"playback"
#define kControlCloud       @"Cloud"
#define kControlMessage     @"message"

@implementation TuyaCameraModule

RCT_EXPORT_MODULE(TuyaCameraModule)


RCT_EXPORT_METHOD(openLivePreview:(NSDictionary *)params resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    NSString *countryCode = params[@"countryCode"];
    NSString *uid = params[@"uid"];
    NSString *passwd = params[@"passwd"];
    NSString *devId = params[@"devId"];
    NSArray *hideCameraOptionsArray = params[@"options"];
    
    if(hideCameraOptionsArray != nil || [hideCameraOptionsArray count] != 0) {
        [[NSUserDefaults standardUserDefaults] setObject:hideCameraOptionsArray forKey:@"OPTIONS_ARRAY"];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }

    [[TuyaSmartUser sharedInstance] loginOrRegisterWithCountryCode:countryCode uid:uid password:passwd createHome:false success:^(id result) {
        [TuyaSmartDevice syncDeviceInfoWithDevId:devId homeId:nil success:^{
          NSLog(@"getToken success");
            
#pragma mark- Pushing to new Camera UI
            CameraAppViewController *vc = (CameraAppViewController *)[TuyaAppViewUtil getCameraStoryBoardControllerForID:@"CameraAppViewController"];
            vc.devId = devId;
            [vc initCamera:devId];
            UIViewController *topVC = [self topViewController];
            
#pragma mark- Pushing to new Camera UI
//            TuyaAppCameraViewController *vc = [[TuyaAppCameraViewController alloc] initWithDeviceId:devId];
//            UIViewController *topVC = [self topViewController];
            
#pragma mark- END
            
          [topVC.navigationController pushViewController:vc animated:YES];
        } failure:^(NSError *error) {
          NSLog(@"Streaming Failiure %@", error);
            [TuyaRNUtils rejecterWithError:error handler:rejecter];
        }];
    } failure:^(NSError *error) {
        [TuyaRNUtils rejecterWithError:error handler:rejecter];
    }];
//    [[TuyaSmartUser sharedInstance] loginByEmail:countryCode email:uid password:passwd success:^{
//        [TuyaSmartDevice syncDeviceInfoWithDevId:devId homeId:nil success:^{
//          NSLog(@"getToken success");
//          TuyaAppCameraViewController *vc = [[TuyaAppCameraViewController alloc] initWithDeviceId:devId];
//          UIViewController *topVC = [self topViewController];
//          [topVC.navigationController pushViewController:vc animated:YES];
//        } failure:^(NSError *error) {
//          NSLog(@"Streaming Failiure %@", error);
//            [TuyaRNUtils rejecterWithError:error handler:rejecter];
//        }];
//    } failure:^(NSError *error) {
//        [TuyaRNUtils rejecterWithError:error handler:rejecter];
//    }];
}

- (UIViewController *) topViewController {
   UIViewController *baseVC = UIApplication.sharedApplication.keyWindow.rootViewController;
   if ([baseVC isKindOfClass:[UINavigationController class]]) {
       return ((UINavigationController *)baseVC).visibleViewController;
   }

   if ([baseVC isKindOfClass:[UITabBarController class]]) {
       UIViewController *selectedTVC = ((UITabBarController*)baseVC).selectedViewController;
       if (selectedTVC) {
           return selectedTVC;
       }
   }

   if (baseVC.presentedViewController) {
       return baseVC.presentedViewController;
   }
   return baseVC;
}

@end
