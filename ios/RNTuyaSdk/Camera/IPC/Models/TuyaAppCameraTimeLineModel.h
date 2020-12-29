//
//  TuyaAppCameraTimeLineModel.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <Foundation/Foundation.h>
#import <TuyaCameraUIKit/TuyaCameraUIKit.h>

@interface TuyaAppCameraTimeLineModel : NSObject <TuyaTimelineViewSource>

@property (nonatomic, assign) NSInteger startTime;

@property (nonatomic, assign) NSInteger stopTime;

@property (nonatomic, strong) NSDate *startDate;

@property (nonatomic, strong) NSDate *stopDate;

- (BOOL)containsPlayTime:(NSInteger)playTime;

@end

