//
//  TuyaSmartCloudTimePieceModel+Timeline.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <TuyaSmartCameraKit/TuyaSmartCameraKit.h>
#import <TuyaCameraUIKit/TuyaCameraUIKit.h>

@interface TuyaSmartCloudTimePieceModel (Timeline)<TuyaTimelineViewSource>

- (BOOL)containsTime:(NSInteger)time;

@end

