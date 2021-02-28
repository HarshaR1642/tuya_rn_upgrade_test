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

@interface CameraPlayBackController () <
TuyaSmartCameraObserver,
TYCameraCalendarViewDelegate,
TYCameraCalendarViewDataSource,
TuyaTimelineViewDelegate,
UITableViewDataSource,
UITableViewDelegate>

@property  (strong, nonatomic) IBOutlet UIView      *controlView;
@property (weak, nonatomic) IBOutlet UIView         *superContentView;
@property (weak, nonatomic) IBOutlet UIView         *playbackContentView;


@property (weak, nonatomic) IBOutlet UIButton       *takePhotoButton;
@property (weak, nonatomic) IBOutlet UIButton       *recordButton;
@property (weak, nonatomic) IBOutlet UIButton       *pauseButton;
@property (weak, nonatomic) IBOutlet UIButton       *muteButton;

@property (weak, nonatomic) IBOutlet UIActivityIndicatorView     *indicatorView;
@property (weak, nonatomic) IBOutlet UILabel                     *stateLabel;
@property (weak, nonatomic) IBOutlet UIButton                    *retryButton;
@property (weak, nonatomic) IBOutlet UITableView                 *playbackTableView;

@property (nonatomic, strong) TuyaSmartPlaybackDate             *currentDate;
@property (nonatomic, strong) TuyaTimelineView                  *timeLineView;
@property (nonatomic, strong) TuyaAppCameraRecordListView       *timeLineListView;

@property (nonatomic, strong) NSArray                                      *timeLineModels;
@property (nonatomic, assign) NSInteger                                    playTime;
@property (nonatomic, strong) TYCameraTimeLabel                            *timeLineLabel;
@property (nonatomic, strong) NSMutableArray<NSNumber *>                   *playbackDays;
@property (nonatomic, strong) TuyaAppCameraCalendarView                    *calendarView;
@property (nonatomic, strong) NSArray<TuyaAppCameraRecordModel *>          *dataSource;
@property (nonatomic, strong) NSIndexPath                                  *selectedIndexPath;
@property (weak, nonatomic) IBOutlet UIView                                *bottomTabControlView;
@property (weak, nonatomic) IBOutlet UILabel *noDataAvialbleLabel;

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
    self.title = self.camera.device.deviceModel.name;
    [self.playbackTableView setBackgroundColor:[TuyaAppTheme theme].navbar_bg_color];
    _playbackContentView.backgroundColor = [UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0];
    [self.view setBackgroundColor:[UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0]];
    [self.bottomTabControlView setBackgroundColor:[UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0]];
    self.addLeftBarBackButtonEnabled = YES;
    [self setRightBarButtonWithImage:@"keyless_filter"];
    self.noDataAvialbleLabel.textColor = [TuyaAppTheme theme].font_color;
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
    [_pauseButton setImage:[TuyaAppViewUtil getOriginalImageFromBundleWithName:@"keyless_pause"] forState: UIControlStateSelected];
}

- (void)didEnterBackground {
    [self.camera stopPlayback];
}

- (void)willEnterForeground {
    [self retryAction];
}

- (void)showAlertWithMessage:(NSString *)msg complete:(void(^)(void))complete {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:msg preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *action = [UIAlertAction actionWithTitle:NSLocalizedString(@"ipc_settings_ok", @"") style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        !complete?:complete();
    }];
    [alert addAction:action];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - Action

- (void)actionRightBarButton: (id)obj {
    [self.view bringSubviewToFront:self.calendarView];
    [self.calendarView show:[NSDate new]];
    TuyaSmartPlaybackDate *playbackDate = [TuyaSmartPlaybackDate new];
    __weak typeof(self) weakSelf = self;
    [self.camera playbackDaysInYear:playbackDate.year month:playbackDate.month complete:^(TYNumberArray *result) {
        weakSelf.playbackDays = [[result.mutableCopy reverseObjectEnumerator] allObjects].mutableCopy;
        [weakSelf.calendarView reloadData];
    }];
}

- (void)checkPhotoPermision:(void(^)(BOOL result))complete {
    if ([TuyaAppPermissionUtil isPhotoLibraryNotDetermined]) {
        [TuyaAppPermissionUtil requestPhotoPermission:complete];
    }else if ([TuyaAppPermissionUtil isPhotoLibraryDenied]) {
        [self showAlertWithMessage:NSLocalizedString(@"Photo library permission denied", @"") complete:nil];
        !complete?:complete(NO);
    }else {
        !complete?:complete(YES);
    }
}

- (IBAction)soundButtonAction:(UIButton *)sender {
    [sender setSelected:!sender.isSelected];
    [self.camera enableMute:!self.camera.isMuted success:^{
        NSLog(@"enable mute success");
//        [sender setSelected: YES];
    } failure:^(NSError *error) {
//        [sender setSelected: NO];
        [self showAlertWithMessage:NSLocalizedString(@"fail", @"") complete:nil];
    }];
}
- (IBAction)recodButtonAction:(UIButton *)sender {
    [self checkPhotoPermision:^(BOOL result) {
        if (result) {
            if (self.camera.isRecording) {
                [self.camera stopRecord:^{
                    [self.recordButton setSelected:NO];
                } failure:^(NSError *error) {
                    [TuyaAppProgressUtils showError:NSLocalizedString(@"record failed", @"")];
                }];
            }else {
                [self.camera startRecord:^{
                    [self.recordButton setSelected:YES];
                } failure:^(NSError *error) {
                    [TuyaAppProgressUtils showError:NSLocalizedString(@"record failed", @"")];
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
        } failure:^(NSError *error) {
            [self showAlertWithMessage:NSLocalizedString(@"fail", @"") complete:nil];
        }];
    }else if (self.camera.isPlaybacking) {
        [self.camera pausePlayback:^{
            [self.pauseButton setSelected:NO];
            self.recordButton.enabled = NO;
        } failure:^(NSError *error) {
            [self showAlertWithMessage:NSLocalizedString(@"fail", @"") complete:nil];
        }];
    }
}

- (IBAction)photoButtonAction:(UIButton *)sender {
    [self checkPhotoPermision:^(BOOL result) {
        if (result) {
            [self.camera snapShoot:^{
                [self showAlertWithMessage:@"A Screenshot has been saved to your photos gallery." complete:nil];
            } failure:^(NSError *error) {
                [self showAlertWithMessage:@"Failed to save" complete:nil];
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
            weakSelf.timeLineView.sourceModels = weakSelf.timeLineModels;
            weakSelf.dataSource = weakSelf.timeLineModels;
            [weakSelf.playbackTableView reloadData];
            [weakSelf.timeLineView setCurrentTime:0 animated:YES];
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
        [TuyaAppProgressUtils showError:NSLocalizedString(@"ipc_errmsg_record_play_failed", @"")];
    }];
}


#pragma mark - TuyaTimelineViewDelegate

- (void)timelineViewWillBeginDragging:(TuyaTimelineView *)timeLineView {
    
}

- (void)timelineViewDidEndDragging:(TuyaTimelineView *)timeLineView willDecelerate:(BOOL)decelerate {
    
}

- (void)timelineViewDidScroll:(TuyaTimelineView *)timeLineView time:(NSTimeInterval)timeInterval isDragging:(BOOL)isDragging {
    self.timeLineLabel.hidden = NO;
    self.timeLineLabel.timeStr = [NSDate tysdk_timeStringWithTimeInterval:timeInterval timeZone:[NSTimeZone localTimeZone]];
}

- (void)timelineView:(TuyaTimelineView *)timeLineView didEndScrollingAtTime:(NSTimeInterval)timeInterval inSource:(id<TuyaTimelineViewSource>)source {
    self.timeLineLabel.hidden = YES;
    if (source) {
        [self playbackWithTime:timeInterval timeLineModel:source];
    }
}

- (NSString *)_durationTimeStampWithStart:(NSDate *)start end:(NSDate *)end {
    NSInteger duration = [end timeIntervalSinceDate:start];
    int h =(int)(duration / 60 / 60);
    int m = (duration / 60) % 60;
    int s = duration % 60;
    return [NSString stringWithFormat:@"%@：%02d:%02d:%02d", NSLocalizedString(@"Duration", @""), h, m, s];
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
        self.playbackDays = [[result.mutableCopy reverseObjectEnumerator] allObjects].mutableCopy;
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

- (void)camera:(TuyaSmartCamera *)camera didReceiveVideoFrame:(CMSampleBufferRef)sampleBuffer frameInfo:(TuyaSmartVideoFrameInfo)frameInfo {
    if (self.playTime != frameInfo.nTimeStamp) {
        self.playTime = frameInfo.nTimeStamp;
        if (!self.timeLineView.isDecelerating && !self.timeLineView.isDragging) {
            [self.timeLineView setCurrentTime:self.playTime];
        }
    }
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


- (TuyaTimelineView *)timeLineView {
    if (!_timeLineView) {
        _timeLineView = [[TuyaTimelineView alloc] initWithFrame:CGRectMake(0, self.controlView.bottom, [UIScreen mainScreen].bounds.size.width, 150)];
        _timeLineView.timeHeaderHeight = 24;
        _timeLineView.showShortMark = YES;
        _timeLineView.spacePerUnit = 90;
        _timeLineView.timeTextTop = 6;
        _timeLineView.delegate = self;
        _timeLineView.backgroundColor = HEXCOLOR(0xf5f5f5);
        _timeLineView.backgroundGradientColors = @[];
        _timeLineView.contentGradientColors = @[(__bridge id)HEXCOLORA(0x4f67ee, 0.62).CGColor, (__bridge id)HEXCOLORA(0x4d67ff, 0.09).CGColor];
        _timeLineView.contentGradientLocations = @[@(0.0), @(1.0)];
        _timeLineView.timeStringAttributes = @{NSFontAttributeName : [UIFont systemFontOfSize:9], NSForegroundColorAttributeName : HEXCOLOR(0x999999)};
        _timeLineView.tickMarkColor = HEXCOLORA(0x000000, 0.1);
        _timeLineView.timeZone = [NSTimeZone localTimeZone];
    }
    return _timeLineView;
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
    return [NSString stringWithFormat:@"%ti H | %ti M | %ti S", [components hour], [components minute], [components second]];
}

- (NSString *)getStringByDate:(NSDate *)date {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"HH:mm a"];
    NSString *stringFromDate = [formatter stringFromDate:date];
    return stringFromDate;
}



- (TYCameraTimeLabel *)timeLineLabel {
    if (!_timeLineLabel) {
        _timeLineLabel = [[TYCameraTimeLabel alloc] initWithFrame:CGRectMake((self.timeLineView.width - 74) / 2, self.timeLineView.top, 74, 22)];
        _timeLineLabel.position = 2;
        _timeLineLabel.hidden = YES;
        _timeLineLabel.ty_backgroundColor = [UIColor blackColor];
        _timeLineLabel.textColor = [UIColor whiteColor];
    }
    return _timeLineLabel;
}


#pragma mark - TableView Delegate Methods


@end
