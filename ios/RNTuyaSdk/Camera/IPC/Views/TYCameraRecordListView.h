//
//  TYCameraRecordListView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>
#import "TuyaAppCameraRecordCell.h"

@class TYCameraRecordListView, TYCameraRecordModel;

@protocol TYCameraRecordListViewDelegate <NSObject>

@optional
- (void)cameraRecordListView:(TuyaAppCameraRecordListView *)listView didSelectedRecord:(NSDictionary *)timeslice;

- (void)cameraRecordListView:(TuyaAppCameraRecordListView *)listView presentCell:(TuyaAppCameraRecordCell *)cell source:(id)source;

@end

@interface TuyaAppCameraEmptyDataView : UIView

@property (nonatomic, strong) UIImageView   *imageView;

@property (nonatomic, strong) UILabel       *textLabel;

@end

@interface TuyaAppCameraRecordListView : UIView<UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSArray *dataSource;

@property (nonatomic, strong) UITableView *tableView;

@property (nonatomic, strong) NSIndexPath *selectedIndexPath;

@property (nonatomic, strong) TuyaAppCameraEmptyDataView *emptyDataView;

@property (nonatomic, weak) id<TYCameraRecordListViewDelegate> delegate;

@end
