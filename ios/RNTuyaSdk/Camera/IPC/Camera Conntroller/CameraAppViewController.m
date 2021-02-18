//
//  CameraAppViewController.m
//  Pods
//
//  Created by Roshan Bisht on 08/02/21.
//

#import "CameraAppViewController.h"
#import "TuyaSmartCameraControlView.h"
#import "TuyaAppPermissionUtil.h"
#import "TuyaAppCameraPlaybackViewController.h"
#import "TuyaAppCameraSettingViewController.h"
#import "TuyaSmartCamera.h"
#import "TuyaAppCameraCloudViewController.h"
#import "TuyaAppCameraMessageViewController.h"
#import "TuyaAppViewConstants.h"
#import "TuyaAppProgressUtils.h"
#import "TuyaAppCameraSDCardViewController.h"
#import "TuyaAppViewUtil.h"
#import "CameraMessageViewController.h"
#import "CameraPlayBackController.h"
#import "CameraSettingsViewController.h"


#define kControlTalk        @"talk"
#define kControlRecord      @"record"
#define kControlPhoto       @"photo"
#define kControlPlayback    @"playback"
#define kControlCloud       @"Cloud"
#define kControlMessage     @"message"

@interface CameraAppViewController ()<TuyaSmartCameraObserver, TuyaSmartCameraDPObserver> {}

@property  (strong, nonatomic) IBOutlet TuyaSmartCameraControlView *controlView;
@property (nonatomic, strong) TuyaSmartCamera *camera;
@property (strong, nonatomic) IBOutlet UIView *contentView;
@property (weak, nonatomic) IBOutlet UIView *superContentView;
@property (weak, nonatomic) IBOutlet UIButton *talkButton;
@property (weak, nonatomic) IBOutlet UIButton *muteButton;
@property (weak, nonatomic) IBOutlet UIButton *retryButton;
@property (weak, nonatomic) IBOutlet UIButton *takePhotoButton;
@property (weak, nonatomic) IBOutlet UIButton *recordButton;
@property (weak, nonatomic) IBOutlet UIButton *playBackButton;
@property (weak, nonatomic) IBOutlet UIButton *messageButton;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *indicatorView;
@property (weak, nonatomic) IBOutlet UILabel *stateLabel;
@property (nonatomic, strong) UIBarButtonItem *rightSettingButton;

@end

@implementation CameraAppViewController

- (void)initCamera:(NSString *)devId {
    if (self) {
        _camera = [[TuyaSmartCamera alloc] initWithDeviceId:devId];
        [_camera.dpManager addObserver:self];
    }
}


#pragma mark - View Life Cycle Methods
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.camera stopPreview];
    [self.camera removeObserver:self];
    [self.camera.dpManager removeObserver:self];
    self.navigationController.navigationBarHidden = YES;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.camera addObserver:self];
    self.navigationController.navigationBarHidden = NO;
    self.navigationItem.rightBarButtonItem = self.rightSettingButton;
    [self.indicatorView setHidden:YES];
    [self.stateLabel setHidden:YES];
    [self.retryButton setHidden: YES];
    [self retryAction];
    self.addLeftBarBackButtonEnabled = YES;
    _superContentView.backgroundColor = [UIColor colorWithRed:55.0/255.0 green:55.0/255.0 blue:55.0/255.0 alpha:1.0];
    
    [[self.navigationController navigationBar] setTintColor:[UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0]];
    [[self.navigationController navigationBar] setBarTintColor:[UIColor colorWithRed:0.0/255.0 green:0.0/255.0 blue:0.0/255.0 alpha:1.0]];
    UIFont *font = [UIFont fontWithName:@"Quicksand-Medium" size:16.0];
    [self.navigationController.navigationBar setTitleTextAttributes: @{NSForegroundColorAttributeName:[UIColor whiteColor], NSFontAttributeName: font}];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setCameraViewTheme];
    self.title = self.camera.device.deviceModel.name;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didEnterBackground) name:UIApplicationDidEnterBackgroundNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(willEnterForeground) name:UIApplicationWillEnterForegroundNotification object:nil];
   
    // Do any additional setup after loading the view.
}

- (void)showLoadingWithTitle:(NSString *)title {
    self.indicatorView.hidden = NO;
    [self.indicatorView startAnimating];
    self.stateLabel.hidden = NO;
    self.stateLabel.text = title;
}

- (void)stopLoading {
    [self.indicatorView stopAnimating];
    self.indicatorView.hidden = YES;
    self.stateLabel.hidden = YES;
}

- (UIBarButtonItem *)rightSettingButton {
    if (!_rightSettingButton) {
        _rightSettingButton = [[UIBarButtonItem alloc] initWithImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_Setting"] imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate] style:UIBarButtonItemStylePlain target:self action:@selector(settingAction)];
    }
    return _rightSettingButton;
}

- (void)rotateDevice {
    CGAffineTransform transform = self.controlView.transform;
    transform = CGAffineTransformConcat(CGAffineTransformScale(transform,  100, 100),
                                        CGAffineTransformRotate(transform, 90));
    [self.controlView setTransform:transform];
}

- (void) settingAction {
    CameraSettingsViewController *settingVC = (CameraSettingsViewController *)[TuyaAppViewUtil getCameraStoryBoardControllerForID:@"CameraSettingsViewController"];
    settingVC.devId = self.devId;
    settingVC.dpManager = self.camera.dpManager;
    [self.navigationController pushViewController:settingVC animated:YES];
    
//    TuyaAppCameraSettingViewController *settingVC = [TuyaAppCameraSettingViewController new];
//    settingVC.devId = self.devId;
//    settingVC.dpManager = self.camera.dpManager;
//    [self.navigationController pushViewController:settingVC animated:YES];
}

#pragma mark - View Themes Setting

- (void)setCameraViewTheme {

    [_talkButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_speak_off"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateNormal];
    
    [_talkButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_speak_on"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateSelected];
    
    
    [_recordButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_record"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateNormal];
    
    [_recordButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_record_on"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateSelected];
    
    
    [_takePhotoButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_photo"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateNormal];
    
    
    [_muteButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_sound_on"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateNormal];
    
    [_muteButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_sound_off"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateSelected];
    
    
    [_messageButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_message"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateNormal];
    
    [_playBackButton setImage:[[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_play_list"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal] forState: UIControlStateNormal];
}

#pragma mark - Camera UI control methods

- (IBAction)playbackButtonAction:(UIButton *)sender {
//    TuyaAppCameraPlaybackViewController *vc = [TuyaAppCameraPlaybackViewController new];
//    vc.camera = self.camera;
//    [self.navigationController pushViewController:vc animated:YES];
    
    
    CameraPlayBackController *messageVC = (CameraPlayBackController *)[TuyaAppViewUtil getCameraStoryBoardControllerForID:@"CameraPlayBackController"];
    messageVC.camera = self.camera;
    [self.navigationController pushViewController:messageVC animated:YES];
}

- (IBAction)messageButtonAction:(UIButton *)sender {
//    TuyaAppCameraMessageViewController *vc = [TuyaAppCameraMessageViewController new];
//    vc.devId = self.devId;
//    [self.navigationController pushViewController:vc animated:YES];

    CameraMessageViewController *messageVC = (CameraMessageViewController *)[TuyaAppViewUtil getCameraStoryBoardControllerForID:@"CameraMessageViewController"];
    messageVC.devId = self.devId;
    [self.navigationController pushViewController:messageVC animated:YES];
}


- (IBAction)soundButtonAction:(UIButton *)sender {
    [sender setSelected:!sender.isSelected];
    [self.camera enableMute:!self.camera.isMuted success:^{
        NSLog(@"enable mute success");
    } failure:^(NSError *error) {
        [self showAlertWithMessage:NSLocalizedString(@"enable mute failed", @"") complete:nil];
    }];
}

- (IBAction)talkButtonAction:(UIButton *)sender {
        [self talkAction];
}

- (IBAction)takePhotoAction:(UIButton *)sender {
    if ([TuyaAppPermissionUtil isPhotoLibraryNotDetermined]) {
        [TuyaAppPermissionUtil requestPhotoPermission:^(BOOL result) {
            if (result) {
                [self photoAction];
            }
        }];
    } else if ([TuyaAppPermissionUtil isPhotoLibraryDenied]) {
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"" message:NSLocalizedString(@"Photo library permission denied", @"") preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:NSLocalizedString(@"ipc_settings_ok", @"") style:UIAlertActionStyleCancel handler:nil];
        [alert addAction:action];
        [self presentViewController:alert animated:YES completion:nil];
    }else {
        [self photoAction];
    }
}

- (IBAction)recordVideoAction:(UIButton *)sender {
    [sender setSelected:!sender.isSelected];
    if ([TuyaAppPermissionUtil isPhotoLibraryNotDetermined]) {
        [TuyaAppPermissionUtil requestPhotoPermission:^(BOOL result) {
            if (result) {
                [self recordAction];
            }
        }];
    } else if ([TuyaAppPermissionUtil isPhotoLibraryDenied]) {
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"" message:NSLocalizedString(@"Photo library permission denied", @"") preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:NSLocalizedString(@"ipc_settings_ok", @"") style:UIAlertActionStyleCancel handler:nil];
        [alert addAction:action];
        [self presentViewController:alert animated:YES completion:nil];
    }else {
        [self recordAction];
    }
}

- (void)photoAction {
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

- (void)recordAction {
    [self checkPhotoPermision:^(BOOL result) {
        if (result) {
            if (self.camera.isRecording) {
                [self.camera stopRecord:^{
                    [TuyaAppProgressUtils showSuccess:@"Video has been saved to your photos gallery." toView:self.view];
                } failure:^(NSError *error) {
                    [TuyaAppProgressUtils showError:@"Failed to save"];
                }];
            }else {
                [self.camera startRecord:^{
                } failure:^(NSError *error) {
                    [TuyaAppProgressUtils showError:@"Failed to save"];
                }];
            }
        }
    }];
}

- (void)talkAction {
    if ([TuyaAppPermissionUtil microNotDetermined]) {
        [TuyaAppPermissionUtil requestAccessForMicro:^(BOOL result) {
            if (result) {
                [self _talkAction];
            }
        }];
    }else if ([TuyaAppPermissionUtil microDenied]) {
        UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"" message:NSLocalizedString(@"Micro permission denied", @"") preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *action = [UIAlertAction actionWithTitle:NSLocalizedString(@"ipc_settings_ok", @"") style:UIAlertActionStyleCancel handler:nil];
        [alert addAction:action];
        [self presentViewController:alert animated:YES completion:nil];
    }else {
        [self _talkAction];
    }
}

- (void)_talkAction {
    if (self.camera.isTalking) {
        [self.camera stopTalk];
        [_talkButton setSelected:FALSE];
        [self.camera enableMute:TRUE success:^{
            NSLog(@"enable mute success");
        } failure:^(NSError *error) {
            [self showAlertWithMessage:NSLocalizedString(@"enable mute failed", @"") complete:nil];
        }];
    }else {
        [self.camera startTalk:^{
            [self.talkButton setSelected:TRUE];
            [self.camera enableMute:FALSE success:^{
                NSLog(@"disable mute success");
            } failure:^(NSError *error) {
                //[self showAlertWithMessage:NSLocalizedString(@"enable mute failed", @"") complete:nil];
            }];
        } failure:^(NSError *error) {
            [TuyaAppProgressUtils showError:NSLocalizedString(@"ipc_errmsg_mic_failed", @"")];
        }];
    }
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

- (void)showAlertWithMessage:(NSString *)msg complete:(void(^)(void))complete {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:nil message:msg preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *action = [UIAlertAction actionWithTitle:NSLocalizedString(@"ipc_settings_ok", @"") style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        !complete?:complete();
    }];
    [alert addAction:action];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - Application entering background/foreground methods
- (void)didEnterBackground {
    [self.camera stopPreview];
    // disconnect p2p channel when enter background
    [self.camera disConnect];
}

- (void)willEnterForeground {
    [self retryAction];
}

- (void)startPreview {
    [self.controlView addSubview:self.camera.videoView];
    self.camera.videoView.frame = self.controlView.bounds;
    self.camera.videoView.scaleToFill = YES;
    [self.camera startPreview:^{
//        [self hideCameraOptions];
        [self stopLoading];
    } failure:^(NSError *error) {
        [self stopLoading];
        self.retryButton.hidden = NO;
    }];
}

- (BOOL)isDoorbell {
    return [self.camera.dpManager isSupportDP:TuyaSmartCameraWirelessAwakeDPName];
}

- (void)retryAction {
//    [self.controlView disableAllControl];
    if (!self.camera.device.deviceModel.isOnline) {
        self.stateLabel.hidden = NO;
        self.stateLabel.text = NSLocalizedString(@"title_device_offline", @"");
        return;
    }
    if ([self isDoorbell]) {
        [self.camera.device awakeDeviceWithSuccess:nil failure:nil];
    }
    [self connectCamera:^(BOOL success) {
        if (success) {
            [self startPreview];
        }else {
            [self stopLoading];
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

- (void)dealloc {
    [self.camera disConnect];
}

#pragma mark - TuyaSmartCameraObserver
- (void)cameraDidDisconnected:(TuyaSmartCamera *)camera {
    [self.controlView disableAllControl];
    self.retryButton.hidden = NO;
}

- (void)camera:(TuyaSmartCamera *)camera didReceiveMuteState:(BOOL)isMuted {
    [_muteButton setSelected:isMuted];
}

- (void)camera:(TuyaSmartCamera *)camera didReceiveDefinitionState:(BOOL)isHd {
    NSString *imageName = @"ty_camera_control_sd_normal";
    if (isHd) {
        imageName = @"ty_camera_control_hd_normal";
    }
//    [self.hdButton setImage:[UIImage imageNamed:imageName] forState:UIControlStateNormal];
}

- (void)camera:(TuyaSmartCamera *)camera didReceiveVideoFrame:(CMSampleBufferRef)sampleBuffer frameInfo:(TuyaSmartVideoFrameInfo)frameInfo {
    
}


@end
