//
//  TuyaAppCameraCalendarView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>

@class TuyaAppCameraCalendarView;

@protocol TYCameraCalendarViewDelegate <NSObject>

//切换月份
- (void)calendarView:(TuyaAppCameraCalendarView *)calendarView didSelectYear:(NSInteger)year month:(NSInteger)month;

//选定日期
- (void)calendarView:(TuyaAppCameraCalendarView *)calendarView didSelectYear:(NSInteger)year month:(NSInteger)month day:(NSInteger)day date:(NSDate *)date;

@end

@protocol TYCameraCalendarViewDataSource <NSObject>

- (BOOL)calendarView:(TuyaAppCameraCalendarView *)calendarView hasVideoOnYear:(NSInteger)year month:(NSInteger)month day:(NSInteger)day;

@end

@interface TuyaAppCameraCalendarView : UIView

- (void)show:(NSDate *)date;
- (void)hide;

- (void)reloadData;

@property (nonatomic, strong) UICollectionView *collectionView;
@property (nonatomic, weak) id<TYCameraCalendarViewDelegate> delegate;
@property (nonatomic, weak) id<TYCameraCalendarViewDataSource> dataSource;

@end

