//
//  CameraMessageViewController.m
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 11/02/21.
//

#import "CameraMessageViewController.h"
#import <TYEncryptImage/TYEncryptImage.h>
#import <SDWebImage/UIImageView+WebCache.h>
#import <TuyaSmartCameraKit/TuyaSmartCameraKit.h>
#import "CameraMessageTableViewCell.h"
#import "TuyaAppTheme.h"
#import "ImagePopUpViewController.h"
#import "MBProgressHUD.h"

@interface CameraMessageViewController () <UITableViewDelegate, UITableViewDataSource, UIGestureRecognizerDelegate>

@property (weak, nonatomic) IBOutlet UITableView                                *messageTableView;
@property (weak, nonatomic) IBOutlet UIView                                     *messageContentView;
@property (nonatomic, strong) TuyaSmartCameraMessage                            *cameraMessage;
@property (nonatomic, strong) NSArray<TuyaSmartCameraMessageSchemeModel *>      *schemeModels;
@property (nonatomic, strong) NSArray<TuyaSmartCameraMessageModel *>            *messageModelList;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint                         *messageTableViewTopConstraint;
@property (strong, nonatomic) UIRefreshControl                                  *refreshControl;
@property (weak, nonatomic) IBOutlet UILabel                                    *noDataLabel;


@end

@implementation CameraMessageViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    [self getMessageScehemes];
    
    _refreshControl = [[UIRefreshControl alloc]init];
    [_refreshControl setTintColor:[TuyaAppTheme theme].button_color];
    self.noDataLabel.textColor = [TuyaAppTheme theme].font_color;
    [self.noDataLabel setHidden:YES];
    [_refreshControl addTarget:self action:@selector(refreshTable) forControlEvents:UIControlEventValueChanged];
    if (@available(iOS 10.0, *)) {
        self.messageTableView.refreshControl = _refreshControl;
    } else {
        [self.messageTableView addSubview:_refreshControl];
    }
    // Do any additional setup after loading the view.
}

- (void)refreshTable {
    [_refreshControl endRefreshing];
    [self getMessageScehemes];
}

- (void)viewWillAppear:(BOOL)animated {
    if (@available(iOS 13.0, *)) {
        [UIApplication sharedApplication].statusBarStyle = UIStatusBarStyleDarkContent;
    }
    [self updateMessageViewConstrants];
}

- (void)updateMessageViewConstrants {
    if (@available(iOS 13, *)) {
        CGFloat topbarHeight = (self.navigationController.navigationBar.frame.size.height ?: 0.0);
        _messageTableViewTopConstraint.constant = topbarHeight;
    }
    [self.view setBackgroundColor: [TuyaAppTheme theme].view_bg_color];
    [_messageTableView setBackgroundColor:[UIColor clearColor]];
    
    [self.topBarView setBackgroundColor:[TuyaAppTheme theme].view_bg_color];
    self.topBarView.leftItem = self.leftBackItem;
    
    [_messageContentView setBackgroundColor:[TuyaAppTheme theme].view_bg_color];
}

- (NSString *)titleForCenterItem {
    return @"Messages";
}

- (void)getMessageScehemes {
    [self.cameraMessage getMessageSchemes:^(NSArray<TuyaSmartCameraMessageSchemeModel *> *result) {
        self.schemeModels = result;
        [self reloadMessageListWithScheme:result.firstObject];
    } failure:^(NSError *error) {
        NSLog(@"error: %@", error);
    }];
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

- (TuyaSmartCameraMessage *)cameraMessage {
    if (!_cameraMessage) {
        _cameraMessage = [[TuyaSmartCameraMessage alloc] initWithDeviceId:self.devId timeZone:[NSTimeZone defaultTimeZone]];
    }
    return _cameraMessage;
}

- (void)reloadMessageListWithScheme:(TuyaSmartCameraMessageSchemeModel *)schemeModel {
    NSDateFormatter *formatter = [NSDateFormatter new];
    formatter.dateFormat = @"yyyy-MM-dd"; 
    NSDate *date = [formatter dateFromString:@"2019-09-17"];
    [self.cameraMessage messagesWithMessageCodes:schemeModel.msgCodes Offset:0 limit:20000 startTime:[date timeIntervalSince1970] endTime:[[NSDate new] timeIntervalSince1970] success:^(NSArray<TuyaSmartCameraMessageModel *> *result) {
        self.messageModelList = result;
        [MBProgressHUD hideHUDForView:self.view animated:YES];
        if (result.count > 0) {
            [self.noDataLabel setHidden:YES];
        } else {
            [self.noDataLabel setHidden:NO];
        }
        [self.messageTableView reloadData];
    } failure:^(NSError *error) {
        NSLog(@"error: %@", error);
    }];
}


#pragma mark- UITable View Delegates

- (nonnull UITableViewCell *)tableView:(nonnull UITableView *)tableView cellForRowAtIndexPath:(nonnull NSIndexPath *)indexPath {
    CameraMessageTableViewCell *messageCell = [tableView dequeueReusableCellWithIdentifier:@"MessgaeCell" forIndexPath:indexPath];
    
    TuyaSmartCameraMessageModel *messageModel = [self.messageModelList objectAtIndex:indexPath.row];
    
    [messageCell.messageUserImageView sd_setImageWithURL:[NSURL URLWithString:messageModel.attachPic]
                 placeholderImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"image_placeholder"]];
    
//    [messageCell.messageUserImageView ty_setAESImageWithPath:messageModel.attachPic encryptKey:@"" placeholderImage:[self placeHolder]];
    
//    [self downloadImageWithURL:messageModel.attachPic onImage:messageCell.messageUserImageView];

    [messageCell.messageTitleLabel setHidden:YES];
    [messageCell.messageDateLabel setText:messageModel.dateTime];
    [messageCell.messageLabel setText:messageModel.msgContent];
    [messageCell.messageLabel setTextColor:[TuyaAppTheme theme].button_color];
    
    messageCell.messageUserImageView.userInteractionEnabled = YES;
    messageCell.messageUserImageView.tag = indexPath.row;
    UITapGestureRecognizer *tapGesture1 = [[UITapGestureRecognizer alloc] initWithTarget:self  action:@selector(tapGesture:)];
    tapGesture1.numberOfTapsRequired = 1;
    [tapGesture1 setDelegate:self];
    [messageCell.messageUserImageView addGestureRecognizer:tapGesture1];
    return messageCell;
}

- (void)tapGesture:(UITapGestureRecognizer*)sender {
    UIImageView *view = (UIImageView *)sender.view;
    TuyaSmartCameraMessageModel *messageModel = [self.messageModelList objectAtIndex:view.tag];
    
    ImagePopUpViewController *popUpVc = (ImagePopUpViewController *)[TuyaAppViewUtil getCameraStoryBoardControllerForID:@"ImagePopUpViewController"];
    popUpVc.imageUrl = messageModel.attachPic;
    [self.navigationController presentViewController:popUpVc animated:YES completion:nil];
}

- (NSInteger)tableView:(nonnull UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.messageModelList.count;
}



- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {

}

//- (void)downloadImageWithURL: (NSString *)url onImage: (UIImageView *)imageView {
//    NSURL *stringUrl = [NSURL URLWithString:url];
//    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
//        @try {
//            NSData *data = [NSData dataWithContentsOfURL:stringUrl];
//            UIImage *image = [UIImage imageWithData:data];
//            dispatch_async(dispatch_get_main_queue(), ^(void){
//                imageView.image = image;
//            });
//        } @catch (NSException *exception) {
//            NSLog(@"%@", exception.reason);
//        }
//
//    });
//}
@end
