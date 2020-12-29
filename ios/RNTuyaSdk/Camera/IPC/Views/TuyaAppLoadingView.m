//
//  TuyaAppLoadingView.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "TuyaAppLoadingView.h"
#import "TuyaAppViewConstants.h"
#import "UIView+TuyaAppAdditions.h"


@implementation TuyaAppLoadingView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        self.frame = CGRectMake(0, 0, APP_CONTENT_WIDTH, 20);
        self.backgroundColor = [UIColor clearColor];
        
        UIActivityIndicatorView *indicatorView = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(APP_CONTENT_WIDTH/2.f - 40, 0, 20, 20)];
        indicatorView.activityIndicatorViewStyle = UIActivityIndicatorViewStyleGray;
        [indicatorView startAnimating];
        [self addSubview:indicatorView];
        
        
        UILabel *textLabel = [[UILabel alloc] initWithFrame:CGRectMake(indicatorView.right + 8, 0, 100, self.height)];
        textLabel.backgroundColor = [UIColor clearColor];
        textLabel.font = [UIFont systemFontOfSize:11];
        textLabel.textColor = [UIColor grayColor];
        
        textLabel.text = TPLocalizedString(@"loading", nil);
        [self addSubview:textLabel];
    }
    return self;
}

@end
