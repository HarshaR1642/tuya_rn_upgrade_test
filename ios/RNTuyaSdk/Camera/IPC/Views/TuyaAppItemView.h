//
//  TuyaAppItemView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>

@class TuyaAppItemView;

@protocol TPItemViewDelegate <NSObject>

@optional

- (void)itemViewTap:(TuyaAppItemView *)itemView;

- (void)itemViewLeftLabelTap:(TuyaAppItemView *)itemView;
- (void)itemViewCenterLabelTap:(TuyaAppItemView *)itemView;
- (void)itemViewRightLabelTap:(TuyaAppItemView *)itemView;

- (void)itemViewLeftImageTap:(TuyaAppItemView *)itemView;
- (void)itemViewCenterImageTap:(TuyaAppItemView *)itemView;
- (void)itemViewRightImageTap:(TuyaAppItemView *)itemView;

@end

@interface TuyaAppItemView : UIView

@property (nonatomic, weak) id<TPItemViewDelegate> delegate;

@property (nonatomic, strong) UILabel *leftLabel;
@property (nonatomic, strong) UILabel *rightLabel;
@property (nonatomic, strong) UILabel *centerLabel;

@property (nonatomic, strong) UIImage *leftImage;
@property (nonatomic, strong) UIImage *centerImage;
@property (nonatomic, strong) UIImage *rightImage;

@property (nonatomic, strong) UIView *topLine;
@property (nonatomic, strong) UIView *middleLine;
@property (nonatomic, strong) UIView *bottomLine;

@property (nonatomic, strong) UIImageView *rightArrow;

@property (nonatomic, strong) UISwitch *switchBtn;

+ (TuyaAppItemView *)itemViewWithFrame:(CGRect)frame;

- (void)showTopLine;
- (void)showMiddleLine;
- (void)showBottomLine;

- (void)showRightArrow;
- (void)showSwitchBtn;

- (void)setRightLabelWidth:(float)width;

@end
