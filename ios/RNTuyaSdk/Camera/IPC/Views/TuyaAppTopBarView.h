//
//  TuyaAppTopBarView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>
#import "TuyaAppBaseView.h"
#import "TuyaAppBarButtonItem.h"

#define TP_LEFT_VIEW_TAG 1200
#define TP_RIGHT_VIEW_TAG 1201
#define TP_CENTER_VIEW_TAG 1202

#define TPTopBarViewTag 35739

@interface TuyaAppTopBarView : TuyaAppBaseView

@property (nonatomic, strong) TuyaAppBarButtonItem *leftItem;
@property (nonatomic, strong) TuyaAppBarButtonItem *centerItem;
@property (nonatomic, strong) TuyaAppBarButtonItem *rightItem;

@property (nonatomic, strong) UIColor           *lineColor;
@property (nonatomic, strong) UIColor           *textColor;

- (void)setTopBarBackgroundColor:(NSArray *)backgroundColorArray;
- (void)setTopBarTextColor:(NSArray *)color;
- (void)setTopBarSubTextColor:(NSArray *)color;
- (void)setTopBarSubTextSelectedColor:(NSArray *)color;

@end
