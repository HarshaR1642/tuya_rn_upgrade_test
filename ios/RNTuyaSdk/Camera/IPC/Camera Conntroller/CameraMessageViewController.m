//
//  CameraMessageViewController.m
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 11/02/21.
//

#import "CameraMessageViewController.h"
#import <TYEncryptImage/TYEncryptImage.h>
//#import <SDWebImage/UIImageView+WebCache.h>
#import <TuyaSmartCameraKit/TuyaSmartCameraKit.h>
#import "CameraMessageTableViewCell.h"

@interface CameraMessageViewController () <UITableViewDelegate, UITableViewDataSource>

@property (weak, nonatomic) IBOutlet UITableView                                *messageTableView;
@property (weak, nonatomic) IBOutlet UIView                                     *messageContentView;
@property (nonatomic, strong) TuyaSmartCameraMessage                            *cameraMessage;
@property (nonatomic, strong) NSArray<TuyaSmartCameraMessageSchemeModel *>      *schemeModels;
@property (nonatomic, strong) NSArray<TuyaSmartCameraMessageModel *>            *messageModelList;

@end

@implementation CameraMessageViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self getMessageScehemes];
    // Do any additional setup after loading the view.
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
    [self.cameraMessage messagesWithMessageCodes:schemeModel.msgCodes Offset:0 limit:20 startTime:[date timeIntervalSince1970] endTime:[[NSDate new] timeIntervalSince1970] success:^(NSArray<TuyaSmartCameraMessageModel *> *result) {
        self.messageModelList = result;
        [self.messageTableView reloadData];
    } failure:^(NSError *error) {
        NSLog(@"error: %@", error);
    }];
}


#pragma mark- UITable View Delegates

- (nonnull UITableViewCell *)tableView:(nonnull UITableView *)tableView cellForRowAtIndexPath:(nonnull NSIndexPath *)indexPath {
    CameraMessageTableViewCell *messageCell = [tableView dequeueReusableCellWithIdentifier:@"MessgaeCell" forIndexPath:indexPath];
    
    TuyaSmartCameraMessageModel *messageModel = [self.messageModelList objectAtIndex:indexPath.row];
    
    [messageCell.messageUserImageView ty_setAESImageWithPath:messageModel.attachPic encryptKey:@"" placeholderImage:[self placeHolder]];

    [messageCell.messageTitleLabel setText:messageModel.msgTitle];
    [messageCell.messageDateLabel setText:messageModel.dateTime];
    [messageCell.messageLabel setText:messageModel.msgContent];
    return messageCell;
}

- (NSInteger)tableView:(nonnull UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.messageModelList.count;
}



- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {

}

@end
