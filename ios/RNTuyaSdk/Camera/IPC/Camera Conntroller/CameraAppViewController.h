//
//  CameraAppViewController.h
//  Pods
//
//  Created by Roshan Bisht on 08/02/21.
//

#import <UIKit/UIKit.h>
#import "TuyaAppBaseViewController.h"

NS_ASSUME_NONNULL_BEGIN


@interface CameraAppViewController : TuyaAppBaseViewController

@property (nonatomic, strong) NSString *devId;
- (void)initCamera:(NSString *)devId;

@end

NS_ASSUME_NONNULL_END
