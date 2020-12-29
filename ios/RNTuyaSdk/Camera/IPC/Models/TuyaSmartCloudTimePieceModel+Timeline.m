//
//  TuyaSmartCloudTimePieceModel+Timeline.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "TuyaSmartCloudTimePieceModel+Timeline.h"

@implementation TuyaSmartCloudTimePieceModel (Timeline)

- (NSTimeInterval)startTimeIntervalSinceDate:(NSDate *)date {
    return [self.startDate timeIntervalSinceDate:date];
}

- (NSTimeInterval)stopTimeIntervalSinceDate:(NSDate *)date {
    return [self.endDate timeIntervalSinceDate:date];
}

- (BOOL)containsTime:(NSInteger)time {
    return time >= self.startTime && time <= self.endTime;
}

@end
