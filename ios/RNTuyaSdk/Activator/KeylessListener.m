//
//  KeylessListener.m
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 23/02/21.
//

#import "KeylessListener.h"


@implementation KeylessListener

NSString *REMOVE_CAMERA = @"REMOVE_CAMERA";

RCT_EXPORT_MODULE(KeylessListener);

- (NSArray<NSString *> *)supportedEvents {
    return @[REMOVE_CAMERA];
}

+ (id)allocWithZone:(NSZone *)zone {
    static KeylessListener *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}


- (void)sendCameraRemoveCommandForDeviceID:(NSString *)deviceID callback:(RCTKeylessListenerCallback)callback {
    if (deviceID) {
        [self sendEventWithName:REMOVE_CAMERA body:@{@"device_id":deviceID}];
        self.keylessListnerCallback = callback;
    }
}


RCT_EXPORT_METHOD(successCallback:(NSString *) command){
    dispatch_async(dispatch_get_main_queue(), ^{
        self.keylessListnerCallback(true, command);
    });
}

RCT_EXPORT_METHOD(failureCallback:(NSString *)message){
    dispatch_async(dispatch_get_main_queue(), ^{
        self.keylessListnerCallback(false, message);
    });
}
@end
