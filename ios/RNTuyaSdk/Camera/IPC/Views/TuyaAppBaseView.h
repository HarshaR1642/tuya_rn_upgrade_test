//
//  TuyaAppBaseView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//
//

#import <UIKit/UIKit.h>
#import "UIView+TuyaAppAdditions.h"

#import "TuyaAppViewUtil.h"
#import "TuyaAppViewConstants.h"

@interface TuyaAppBaseView : UIView

@property (nonatomic, assign) BOOL    topLineHidden;
@property (nonatomic, assign) float   topLineWidth;
@property (nonatomic, assign) float   topLineIndent;
@property (nonatomic, strong) UIColor *topLineColor;

@property (nonatomic, assign) BOOL    bottomLineHidden;
@property (nonatomic, assign) float   bottomLineWidth;
@property (nonatomic, assign) float   bottomLineIndent;
@property (nonatomic, strong) UIColor *bottomLineColor;

@end

NSString *UIKitLocalizedString(NSString *string);
