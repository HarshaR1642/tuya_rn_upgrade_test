//
//  TuyaAppCellView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//
//

#import "TuyaAppBaseView.h"
#import "TuyaAppCellViewItem.h"

@class TuyaAppCellView;

@protocol TuyaAppCellViewDelegate <NSObject>

- (void)TPCellViewTap:(TuyaAppCellView *)TPCellView;

@end

@interface TuyaAppCellView : TuyaAppBaseView

@property (nonatomic, weak) id<TuyaAppCellViewDelegate> delegate;

@property (nonatomic, assign) BOOL roundCorner;

@property (nonatomic, strong) TuyaAppCellViewItem *leftItem;
@property (nonatomic, strong) TuyaAppCellViewItem *centerItem;
@property (nonatomic, strong) TuyaAppCellViewItem *rightItem;

+ (TuyaAppCellView *)seperateCellView:(CGRect)frame  backgroundColor:(UIColor *)backgroundColor;

- (void)showRightArrow;

@end
