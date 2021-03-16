//
//  KeylessListener.h
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 23/02/21.
//

#import <React/RCTEventEmitter.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^RCTKeylessListenerCallback)(BOOL success, NSString *errorMessage);

@interface KeylessListener : RCTEventEmitter <RCTBridgeModule>

- (void)sendCameraRemoveCommandForDeviceID:(NSString *)deviceID callback:(RCTKeylessListenerCallback) callback;


@property (nonatomic, copy) RCTKeylessListenerCallback keylessListnerCallback;

@end

NS_ASSUME_NONNULL_END
