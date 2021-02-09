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


#define kControlTalk        @"talk"
#define kControlRecord      @"record"
#define kControlPhoto       @"photo"
#define kControlPlayback    @"playback"
#define kControlCloud       @"Cloud"
#define kControlMessage     @"message"

@interface CameraAppViewController ()<TuyaSmartCameraObserver, TuyaSmartCameraControlViewDelegate, TuyaSmartCameraDPObserver> {}

@property  (strong, nonatomic) IBOutlet TuyaSmartCameraControlView *controlView;
@property (nonatomic, strong) TuyaSmartCamera *camera;
@property (strong, nonatomic) IBOutlet UIView *contentView;

@property (strong, nonatomic) IBOutlet UIButton *talkButton;
@property (strong, nonatomic) IBOutlet UIButton *muteButton;
@property (strong, nonatomic) IBOutlet UIButton *takePhotoButton;
@property (strong, nonatomic) IBOutlet UIButton *recordButton;
@property (strong, nonatomic) IBOutlet UIButton *playBackButton;
@property (strong, nonatomic) IBOutlet UIButton *messageButton;

@end

@implementation CameraAppViewController

- (void)initCamera:(NSString *)devId {
    if (self) {
        _camera = [[TuyaSmartCamera alloc] initWithDeviceId:devId];
        [_camera.dpManager addObserver:self];
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self retryAction];
    [self setCameraViewTheme];
    self.navigationController.navigationBar.backgroundColor = UIColor.brownColor;
    self.title = NSLocalizedString(@"ipc_panel_button_settings", @"");
    // Do any additional setup after loading the view.
}

#pragma mark - View Themes Setting

- (void)setCameraViewTheme {
    [_talkButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_speak_on"] forState: UIControlStateNormal];
    [_talkButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_speak_off"] forState: UIControlStateSelected];
    
    [_recordButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_record"] forState: UIControlStateNormal];
    [_recordButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_record_on"] forState: UIControlStateSelected];
    
    [_takePhotoButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_photo"] forState: UIControlStateNormal];
    
    [_muteButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_sound_off"] forState: UIControlStateNormal];
    [_muteButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_sound_on"] forState: UIControlStateSelected];
    
    [_messageButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_message"] forState: UIControlStateNormal];
    
    [_playBackButton setImage:[TuyaAppViewUtil getImageFromBundleWithName:@"keyless_play_list"] forState: UIControlStateNormal];
}

#pragma mark - Camera UI control methods



#pragma mark - View Life Cycle Methods
- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.camera stopPreview];
    [self.camera removeObserver:self];
    [self.camera.dpManager removeObserver:self];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.camera addObserver:self];
    
    [self retryAction];
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
//        [self stopLoading];
    } failure:^(NSError *error) {
//        [self stopLoading];
//        self.retryButton.hidden = NO;
    }];
}

- (BOOL)isDoorbell {
    return [self.camera.dpManager isSupportDP:TuyaSmartCameraWirelessAwakeDPName];
}

- (void)retryAction {
//    [self.controlView disableAllControl];
    if (!self.camera.device.deviceModel.isOnline) {
//        self.stateLabel.hidden = NO;
//        self.stateLabel.text = NSLocalizedString(@"title_device_offline", @"");
        return;
    }
    if ([self isDoorbell]) {
        [self.camera.device awakeDeviceWithSuccess:nil failure:nil];
    }
    [self connectCamera:^(BOOL success) {
        if (success) {
            [self startPreview];
        }else {
//            [self stopLoading];
//            self.retryButton.hidden = NO;
        }
    }];
//    [self showLoadingWithTitle:NSLocalizedString(@"loading", @"")];
//    self.retryButton.hidden = YES;
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
//    self.retryButton.hidden = NO;
}

- (void)camera:(TuyaSmartCamera *)camera didReceiveMuteState:(BOOL)isMuted {
    NSString *imageName = @"ty_camera_soundOn_icon";
    if (isMuted) {
        imageName = @"ty_camera_soundOff_icon";
    }
//    [self.soundButton setImage:[UIImage imageNamed:imageName] forState:UIControlStateNormal];
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


#pragma mark - TuyaSmartCameraControlViewDelegate

- (void)controlView:(TuyaSmartCameraControlView *)controlView didSelectedControl:(NSString *)identifier {
    if ([identifier isEqualToString:kControlTalk]) {
//        [self talkAction];
        return;
    }
    if ([identifier isEqualToString:kControlPlayback]) {
        TuyaAppCameraPlaybackViewController *vc = [TuyaAppCameraPlaybackViewController new];
//        vc.camera = self.camera;
        [self.navigationController pushViewController:vc animated:YES];
        return;
    }
    if ([identifier isEqualToString:kControlCloud]) {
        TuyaAppCameraCloudViewController *vc = [TuyaAppCameraCloudViewController new];
//        vc.devId = self.devId;
        [self.navigationController pushViewController:vc animated:YES];
    }
    if ([identifier isEqualToString:kControlMessage]) {
        TuyaAppCameraMessageViewController *vc = [TuyaAppCameraMessageViewController new];
//        vc.devId = self.devId;
        [self.navigationController pushViewController:vc animated:YES];
    }
    BOOL needPhotoPermission = [identifier isEqualToString:kControlPhoto] || [identifier isEqualToString:kControlRecord];
    if (needPhotoPermission) {
        if ([TuyaAppPermissionUtil isPhotoLibraryNotDetermined]) {
            [TuyaAppPermissionUtil requestPhotoPermission:^(BOOL result) {
                if (result) {
                    if ([identifier isEqualToString:kControlRecord]) {
//                        [self recordAction];
                    } else if ([identifier isEqualToString:kControlPhoto]) {
//                        [self photoAction];
                    }
                }
            }];
        }else if ([TuyaAppPermissionUtil isPhotoLibraryDenied]) {
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"" message:NSLocalizedString(@"Photo library permission denied", @"") preferredStyle:UIAlertControllerStyleAlert];
            UIAlertAction *action = [UIAlertAction actionWithTitle:NSLocalizedString(@"ipc_settings_ok", @"") style:UIAlertActionStyleCancel handler:nil];
            [alert addAction:action];
            [self presentViewController:alert animated:YES completion:nil];
        }else {
            if ([identifier isEqualToString:kControlRecord]) {
//                [self recordAction];
            } else if ([identifier isEqualToString:kControlPhoto]) {
//                [self photoAction];
            }
        }
    }
}
@end
