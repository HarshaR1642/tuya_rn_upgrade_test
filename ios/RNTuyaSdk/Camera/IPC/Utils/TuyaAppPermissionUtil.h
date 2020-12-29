//
//  TYPermissionUtil.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <Foundation/Foundation.h>
#import <TuyaSmartBaseKit/TuyaSmartBaseKit.h>

@interface TuyaAppPermissionUtil : NSObject

+ (BOOL)isPhotoLibraryDenied;
+ (BOOL)isPhotoLibraryNotDetermined;
+ (void)requestPhotoPermission:(TYSuccessBOOL)result;

+ (BOOL)microNotDetermined;
+ (BOOL)microDenied;
+ (void)requestAccessForMicro:(TYSuccessBOOL)result;

+ (void)showAppSettingsTip:(NSString *)tip;

@end
