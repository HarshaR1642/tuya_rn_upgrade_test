//
//  CameraSettingsViewController.m
//  Base64
//
//  Created by Roshan Bisht on 11/02/21.
//

#import "CameraPlayBackController.h"
#import "TuyaAppPermissionUtil.h"
#import "TuyaAppCameraCalendarView.h"
#import "TuyaAppViewConstants.h"
#import "TuyaAppProgressUtils.h"
#import "TuyaAppCameraTimeLineModel.h"
#import <YYModel/YYModel.h>
#import "TuyaSmartCamera.h"
#import "TuyaAppCameraRecordListView.h"
#import "TuyaAppTheme.h"
#import "CameraPlaybackTableViewCell.h"
#import "TuyaAppPermissionUtil.h"
#import "TuyaAppProgressUtils.h"
#import "FCAlertView.h"

@interface CameraPlayBackController () <
TuyaSmartCameraObserver,
TYCameraCalendarViewDelegate,
TYCameraCalendarViewDataSource,
UITableViewDataSource,
UITableViewDelegate>

@property  (strong, nonatomic) IBOutlet UIView                             *controlView;
@property (weak, nonatomic) IBOutlet UIView                                *superContentView;
@property (weak, nonatomic) IBOutlet UIView                                *playbackContentView;


@property (weak, nonatomic) IBOutlet UIButton                              *takePhotoButton;
@property (weak, nonatomic) IBOutlet UIButton                              *recordButton;
@property (weak, nonatomic) IBOutlet UIButton                              *pauseButton;
@property (weak, nonatomic) IBOutlet UIButton                              *muteButton;
                    
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView               *indicatorView;
@property (weak, nonatomic) IBOutlet UILabel                               *stateLabel;
@property (weak, nonatomic) IBOutlet UIButton                              *retryButton;
@property (weak, nonatomic) IBOutlet UITableView                           *playbackTableView;

@property (nonatomic, strong) TuyaSmartPlaybackDate                        *currentDate;
@property (nonatomic, strong) TuyaAppCameraRecordListView                  *timeLineListView;

@property (nonatomic, strong) NSArray                                      *timeLineModels;
@property (nonatomic, assign) NSInteger                                    playTime;
@property (nonatomic, strong) NSMutableArray<NSNumber *>                   *playbackDays;
@property (nonatomic, strong) TuyaAppCameraCalendarView                    *calendarView;
@property (nonatomic, strong) NSArray<TuyaAppCameraRecordModel *>          *dataSource;
@property (nonatomic, strong) NSIndexPath                                  *selectedIndexPath;
@property (weak, nonatomic) IBOutlet UIView                                *bottomTabControlView;
@property (weak, nonatomic) IBOutlet UILabel                               *noDataAvialbleLabel;

@end

@implementation CameraPlayBackController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didEnterBackground) name:UIApplicationDidEnterBackgroundNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(willEnterForeground) name:UIApplicationWillEnterForegroundNotification object:nil];
    // Do any additional setup after loading the view.
}


- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.camera addObserver:self];
    [self setCameraViewTheme];
    [self.view addSubview:self.calendarView];
    self.navigationController.navigationBarHidden = NO;
    [self.indicatorView setHidden:YES];
    [self.stateLabel setHidden:YES];
    [self.retryButton setHidden: YES];
    [self retryAction];
//    self.title = self.camera.device.deviceModel.name;
    self.title = NSLocalizedString(@"playbackScreen", @"");
    [self.playbackTableView setBackgroundColor:[TuyaAppTheme theme].navbar_bg_color];
    _playbackContentView.backgroundColor = [UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0];
    [self.view setBackgroundColor:[UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0]];
    [self.bottomTabControlView setBackgroundColor:[UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0]];
    self.addLeftBarBackButtonEnabled = YES;
    [self setRightBarButtonWithImage:@"calendar"];
    self.camera.videoView.scaleToFill = YES;
    self.noDataAvialbleLabel.textColor = [TuyaAppTheme theme].font_color;
    self.noDataAvialbleLabel.text = NSLocalizedString(@"ipc_playback_no_records_today", @"");
}



- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.camera removeObserver:self];
    [self.camera stopPlayback];
}


- (void)setCameraViewTheme {
    [_recordButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_record"] forState: UIControlStateNormal];
    [_recordButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_record_on"] forState: UIControlStateSelected];

    [_takePhotoButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_photo"] forState: UIControlStateNormal];
    
    [_muteButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_sound_off"] forState: UIControlStateNormal];
    
    [_muteButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_sound_on"] forState: UIControlStateSelected];
    
    [_pauseButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_pause"] forState: UIControlStateNormal];
    [_pauseButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_play_playback"] forState: UIControlStateSelected];
}

- (void)didEnterBackground {
    [self.camera stopPlayback];
}

- (void)willEnterForeground {
    [self retryAction];
}

#pragma mark - Action

- (void)actionRightBarButton: (id)obj {
    [self.view bringSubviewToFront:self.calendarView];
    [self.calendarView show:[NSDate new]];
    TuyaSmartPlaybackDate *playbackDate = [TuyaSmartPlaybackDate new];
    __weak typeof(self) weakSelf = self;
    [self.camera playbackDaysInYear:playbackDate.year month:playbackDate.month complete:^(TYNumberArray *result) {
        weakSelf.playbackDays = result.mutableCopy;
        [weakSelf.calendarView reloadData];
    }];
}

- (void)checkPhotoPermision:(void(^)(BOOL result))complete {
    if ([TuyaAppPermissionUtil isPhotoLibraryNotDetermined]) {
        [TuyaAppPermissionUtil requestPhotoPermission:complete];
    }else if ([TuyaAppPermissionUtil isPhotoLibraryDenied]) {
        
        FCAlertView *alert = [[FCAlertView alloc] init];
        [alert showAlertInView:self
                     withTitle:@""
                  withSubtitle:[NSString stringWithFormat:@"%@ %@", [TuyaAppTheme app_name], NSLocalizedString(@"photo_permission_denied", @"")]
               withCustomImage:nil
           withDoneButtonTitle: NSLocalizedString(@"settings", @"")
                    andButtons:nil];
        [alert addButton:NSLocalizedString(@"cancel", @"") withActionBlock:nil];
        [alert doneActionBlock:^{
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        }];
        
        !complete?:complete(NO);
    }else {
        !complete?:complete(YES);
    }
}

- (IBAction)soundButtonAction:(UIButton *)sender {
    [sender setSelected:!sender.isSelected];
    [self.camera enableMute:!self.camera.isMuted success:^{
//        [sender setSelected: YES];
    } failure:^(NSError *error) {
//        [sender setSelected: NO];
        [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"mute_failed", @"") withTitle:@""];
    }];
}
- (IBAction)recodButtonAction:(UIButton *)sender {
    [self checkPhotoPermision:^(BOOL result) {
        if (result) {
            if (self.camera.isRecording) {
                [self.camera stopRecord:^{
                    [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"video_saved", @"") withTitle:NSLocalizedString(@"success", @"")];
                    [self.recordButton setSelected:NO];
                } failure:^(NSError *error) {
                    [self.recordButton setSelected:NO];
                    [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"failed_record_playback", @"") withTitle:@""];
                }];
            }else {
                [self.camera startRecord:^{
                    [self.recordButton setSelected:YES];
                } failure:^(NSError *error) {
                    [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"failed_record_playback", @"") withTitle:@""];
                    [self.recordButton setSelected:YES];
                }];
            }
        }
    }];
}

- (IBAction)pauseButtonAction:(UIButton *)sender {
    if (self.camera.isPlaybackPuased) {
        [self.camera resumePlayback:^{
            [self enableAllControl:YES];
            [self stopLoadingWithText:nil];
            [self.pauseButton setSelected:YES];
            [self.pauseButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_pause"] forState: UIControlStateSelected];
        } failure:^(NSError *error) {
            [self.pauseButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_play_playback"] forState: UIControlStateNormal];
            [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"pause_fail", @"") withTitle:@""];
        }];
    }else if (self.camera.isPlaybacking) {
        [self.camera pausePlayback:^{
            [self.pauseButton setSelected:NO];
            self.recordButton.enabled = NO;
            [self.pauseButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_play_playback"] forState: UIControlStateNormal];
        } failure:^(NSError *error) {
            [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"resume_fail", @"") withTitle:@""];
            [self.pauseButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_pause"] forState: UIControlStateSelected];
        }];
    }
}

- (IBAction)photoButtonAction:(UIButton *)sender {
    [self checkPhotoPermision:^(BOOL result) {
        if (result) {
            [self.camera snapShoot:^{
                [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"photo_save", @"") withTitle:NSLocalizedString(@"success", @"")];
            } failure:^(NSError *error) {
                [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"photo_save_fail", @"") withTitle:@""];
            }];
        }
    }];
}


- (void)retryAction {
    [self connectCamera:^(BOOL success) {
        if (success) {
            [self startPlayback];
        }else {
            [self stopLoadingWithText:@""];
            self.retryButton.hidden = NO;
        }
    }];
    [self showLoadingWithTitle:NSLocalizedString(@"loading", @"")];
    self.retryButton.hidden = YES;
}

- (void)connectCamera:(void(^)(BOOL success))complete {
    if (self.camera.isConnecting) return;
    if (self.camera.isConnected) {
        complete(YES);
        return;
    }
    [self.camera connect:^{
        complete(YES);
    } failure:^(NSError *error) {
        complete(NO);
    }];
}

- (void)showLoadingWithTitle:(NSString *)title {
    self.indicatorView.hidden = NO;
    [self.indicatorView startAnimating];
    self.stateLabel.hidden = NO;
    self.stateLabel.text = title;
}

- (void)stopLoadingWithText:(NSString *)text {
    [self.indicatorView stopAnimating];
    self.indicatorView.hidden = YES;
    if (text.length > 0) {
        self.stateLabel.text = text;
    }else {
        self.stateLabel.hidden = YES;
    }
}

- (void)startPlayback {
    [self.camera.videoView tuya_clear];
    self.camera.videoView.scaleToFill = YES;
    [self.controlView addSubview:self.camera.videoView];
    self.camera.videoView.frame = self.controlView.bounds;
    [self getRecordAndPlay:[TuyaSmartPlaybackDate new]];
}

- (void)enableAllControl:(BOOL)enabled {
    self.takePhotoButton.enabled = enabled;
    self.pauseButton.enabled = enabled;
    self.recordButton.enabled = enabled;
}

- (void)getRecordAndPlay:(TuyaSmartPlaybackDate *)playbackDate {
    self.title = [NSString stringWithFormat:@"%@-%@-%@", @(playbackDate.year), @(playbackDate.month), @(playbackDate.day)];
    self.currentDate = playbackDate;
    __weak typeof(self) weakSelf = self;
    [self showLoadingWithTitle:@""];
    [self.camera requestTimeSliceWithPlaybackDate:playbackDate complete:^(TYDictArray *result) {
        if (result.count > 0) {
            weakSelf.timeLineModels = [NSArray yy_modelArrayWithClass:[TuyaAppCameraTimeLineModel class] json:result];
            weakSelf.dataSource = [[weakSelf.timeLineModels reverseObjectEnumerator] allObjects].mutableCopy;
            TuyaAppCameraTimeLineModel *timeslice = (TuyaAppCameraTimeLineModel *)[weakSelf.dataSource objectAtIndex:0];
            if (timeslice) {
                TuyaAppCameraTimeLineModel *model = (TuyaAppCameraTimeLineModel*)timeslice;
                [self playbackWithTime:model.startTime timeLineModel:model];
                weakSelf.selectedIndexPath = [NSIndexPath indexPathForRow:0 inSection:0];
            }
            [weakSelf.playbackTableView reloadData];
            [weakSelf.noDataAvialbleLabel setHidden:YES];
        }else {
            [weakSelf.noDataAvialbleLabel setHidden:NO];
            [weakSelf stopLoadingWithText:NSLocalizedString(@"ipc_playback_no_records_today", @"")];
        }
    }];
}

- (void)playbackWithTime:(NSInteger)playTime timeLineModel:(TuyaAppCameraTimeLineModel *)model {
    playTime = [model containsPlayTime:playTime] ? playTime : model.startTime;
    [self showLoadingWithTitle:@""];
    [self.camera startPlaybackWithPlayTime:playTime timelineModel:model success:^{
        [self enableAllControl:YES];
        [self stopLoadingWithText:@""];
    } failure:^(NSError *error) {
        [TuyaAppProgressUtils showAlertForView:self withMessage:NSLocalizedString(@"ipc_errmsg_record_play_failed", @"") withTitle:@"Failed"];
    }];
}

#pragma mark - TYCameraCalendarViewDataSource

- (BOOL)calendarView:(TuyaAppCameraCalendarView *)calendarView hasVideoOnYear:(NSInteger)year month:(NSInteger)month day:(NSInteger)day {
    if (!self.playbackDays) {
        return NO;
    }
    return [self.playbackDays containsObject:@(day)];
}

#pragma mark - TYCameraCalendarViewDelegate

- (void)calendarView:(TuyaAppCameraCalendarView *)calendarView didSelectYear:(NSInteger)year month:(NSInteger)month {
    self.playbackDays = nil;
    [self showLoadingWithTitle:@""];
    [self.camera playbackDaysInYear:year month:month complete:^(TYNumberArray *result) {
        [self stopLoadingWithText:@""];
        self.playbackDays = result.mutableCopy;
        [calendarView reloadData];
    }];
}

- (void)calendarView:(TuyaAppCameraCalendarView *)calendarView didSelectYear:(NSInteger)year month:(NSInteger)month day:(NSInteger)day date:(NSDate *)date {
    [calendarView hide];
    [self getRecordAndPlay:[TuyaSmartPlaybackDate playbackDateWithDate:date]];
}

#pragma mark - TuyaSmartCameraObserver

- (void)cameraPlaybackDidFinished:(TuyaSmartCamera *)camera {
    [self enableAllControl:NO];
    [self stopLoadingWithText:NSLocalizedString(@"ipc_video_end", @"")];
}

- (void)camera:(TuyaSmartCamera *)camera didReceiveMuteState:(BOOL)isMuted {
    [_muteButton setSelected:!isMuted];
}


#pragma mark - Accessor

- (TuyaAppCameraCalendarView *)calendarView {
    if (!_calendarView) {
        _calendarView = [[TuyaAppCameraCalendarView alloc] initWithFrame:CGRectZero];
        _calendarView.dataSource = self;
        _calendarView.delegate = self;
    }
    return _calendarView;
}


#pragma mark - UITableViewDataSourceDelegate

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return _dataSource.count ? _dataSource.count : 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    CameraPlaybackTableViewCell *cell = (CameraPlaybackTableViewCell *)[tableView dequeueReusableCellWithIdentifier:@"PlaybackCell" forIndexPath: indexPath];
    TuyaAppCameraTimeLineModel *timeslice = (TuyaAppCameraTimeLineModel *)[self.dataSource objectAtIndex:indexPath.row];
    
    [cell setBackgroundColor:[UIColor clearColor]];
    [cell.playbackTitleLabel setText:[self getStringByDate: timeslice.startDate]];
    [cell.playbackTitleLabel setTextColor:[TuyaAppTheme theme].button_color];
    [cell.playbackMessageImageView setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_play"]];
    [cell.playbackMessageImageView setTintColor:[TuyaAppTheme theme].button_color];
    [cell.playbackMessageLabel setText:[self duration:timeslice.startDate endDate:timeslice.stopDate]];
    
    [cell.playbackMessageView setBackgroundColor:[UIColor whiteColor]];
    if (indexPath == _selectedIndexPath) {
        [cell.playbackMessageView setBackgroundColor:[TuyaAppTheme theme].cell_selected_color];
    }
    cell.playbackMessageView.layer.cornerRadius = 5.0;
    cell.playbackMessageView.clipsToBounds = true;
    
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 60;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if ([_selectedIndexPath isEqual:indexPath]) {
        return;
    }
    if (_selectedIndexPath) {
        [tableView deselectRowAtIndexPath:_selectedIndexPath animated:YES];
    }
    _selectedIndexPath = indexPath;
    TuyaAppCameraTimeLineModel *timeslice = (TuyaAppCameraTimeLineModel *)[self.dataSource objectAtIndex:indexPath.row];
    if (timeslice) {
        TuyaAppCameraTimeLineModel *model = (TuyaAppCameraTimeLineModel*)timeslice;
        [self playbackWithTime:model.startTime timeLineModel:model];
    }
    [tableView reloadData];
    
}
- (NSString *)duration:(NSDate *)startDate endDate:(NSDate *)endDate
{
    NSCalendarUnit units = NSCalendarUnitDay | NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond;
    NSDateComponents *components = [[NSCalendar currentCalendar] components:units fromDate: startDate toDate: endDate options: 0];
    
    return [NSString stringWithFormat:@"Duration: %02ld:%02ld:%02ld", (long)[components hour], (long)[components minute], (long)[components second]];
//     [NSString stringWithFormat:@"%ti H | %ti M | %ti S", [components hour], [components minute], [components second]];
}

- (NSString *)getStringByDate:(NSDate *)date {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"HH:mm a"];
    NSString *stringFromDate = [formatter stringFromDate:date];
    return stringFromDate;
}


#pragma mark - TableView Delegate Methods


@end
