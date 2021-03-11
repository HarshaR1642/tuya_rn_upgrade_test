//
//  ImagePopUpViewController.m
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 27/02/21.
//

#import "ImagePopUpViewController.h"
#import "TuyaAppTheme.h"
#import <SDWebImage/UIImageView+WebCache.h>

@interface ImagePopUpViewController ()<UIScrollViewDelegate>

@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;

@end

@implementation ImagePopUpViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.imageView sd_setImageWithURL:[NSURL URLWithString:_imageUrl] placeholderImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"image_placeholder"]];
    self.scrollView.minimumZoomScale=1.0;
    self.scrollView.maximumZoomScale=4.0;
    self.scrollView.delegate = self;
    self.topBarView.backgroundColor = [TuyaAppTheme theme].view_bg_color;
//    self.topBarView.rightItem = self.rightCancelItem;
    [self.view setBackgroundColor:[UIColor blackColor]];
    self.scrollView.showsVerticalScrollIndicator = NO;
    self.scrollView.showsHorizontalScrollIndicator = NO;
    // Do any additional setup after loading the view.
}

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView {
    return self.imageView;
}

- (UIImage *)placeHolder {
    static UIImage *image = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        UIGraphicsBeginImageContext(CGSizeMake(88, 50));
        image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
    });
    return image;
}

@end
