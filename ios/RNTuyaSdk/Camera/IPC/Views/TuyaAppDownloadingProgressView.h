//
//  TuyaAppDownloadingProgressView.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>

@interface TuyaAppDownloadingProgressView : UIView

@property (nonatomic, assign) NSInteger progress;

@property (nonatomic, copy) void(^cancelAction)(void);

- (void)show;

- (void)hide;

- (void)hideProgressLabel;

@end
