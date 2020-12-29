//
//  UIViewController+ATCategory.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "UIViewController+TuyaAppCategory.h"
#import "TuyaAppViewConstants.h"

@implementation UIViewController (TuyaAppCategory)


- (void)tp_dismissModalViewController {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (BOOL)tp_isModal {
    if ([self presentingViewController])
        return YES;
    if ([[[self navigationController] presentingViewController] presentedViewController] == [self navigationController])
        return YES;
    if ([[[self tabBarController] presentingViewController] isKindOfClass:[UITabBarController class]])
        return YES;
    
    return NO;
}



- (void)tp_dismissCurrentPresentedControllerAnimated:(BOOL)animated completion:(void (^)(void))completion {
    if (self.presentedViewController) {
        [self dismissViewControllerAnimated:animated completion:completion];
    } else {
        if (completion) {
            completion();
        }
    }
}

- (UIViewController *)tp_currentPresentedController {
    return self.presentedViewController;
}


- (UIViewController *)tp_currentPresentingController {
    return self.presentingViewController;
}


@end
