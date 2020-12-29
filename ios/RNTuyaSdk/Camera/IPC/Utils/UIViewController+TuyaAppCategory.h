//
//  UIViewController+ATCategory.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>

@interface UIViewController (TuyaAppCategory)

- (void)tp_dismissModalViewController;

- (BOOL)tp_isModal;

- (void)tp_dismissCurrentPresentedControllerAnimated:(BOOL)animated completion:(void (^)(void))completion;

- (UIViewController *)tp_currentPresentedController;

- (UIViewController *)tp_currentPresentingController;
@end
