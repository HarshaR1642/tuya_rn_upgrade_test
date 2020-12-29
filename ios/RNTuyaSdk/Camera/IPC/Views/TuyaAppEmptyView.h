//
//  TuyaAppEmptyView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>
#import "TuyaAppBaseView.h"

@interface TuyaAppEmptyView : UIView

@property (nonatomic, strong) UILabel *titleLabel;

#pragma mark - Style1

- (id)initWithFrame:(CGRect)frame title:(NSString *)title imageName:(NSString *)imageName;

- (void)setTitle:(NSString *)title;

#pragma mark - Style2

- (id)initWithFrame:(CGRect)frame title:(NSString *)title subTitle:(NSString *)subTitle;

- (void)setTitle:(NSString *)title subTitle:(NSString *)subTitle;

@end
