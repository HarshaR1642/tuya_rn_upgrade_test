//
//  CameraAppViewController.h
//  Pods
//
//  Created by Roshan Bisht on 08/02/21.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface CameraAppViewController : UIViewController
@property (nonatomic, strong) NSString *devId;
- (void)initCamera:(NSString *)devId;

@end

NS_ASSUME_NONNULL_END
