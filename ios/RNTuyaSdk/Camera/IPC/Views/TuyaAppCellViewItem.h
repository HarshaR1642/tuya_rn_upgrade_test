//
//  TuyaAppCellViewItem.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface TuyaAppCellViewItem : NSObject

@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) UIColor  *textColor;
@property (nonatomic, assign) float    fontSize;

@property (nonatomic, strong) UIImage *image;

@property (nonatomic, strong) UIView *customView;

+ (TuyaAppCellViewItem *)cellItemWithTitle:(NSString *)title image:(UIImage *)image;
+ (TuyaAppCellViewItem *)cellItemWithArrowImage:(NSString *)title;

@end
