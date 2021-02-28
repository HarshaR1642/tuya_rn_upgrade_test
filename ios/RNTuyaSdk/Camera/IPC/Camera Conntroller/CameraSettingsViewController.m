//
//  CameraSettingsViewController.m
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 16/02/21.
//

#import "CameraSettingsViewController.h"
#import "TuyaAppCameraSDCardViewController.h"
#import "TuyaAppProgressUtils.h"
#import "CameraSettingsTableViewCell.h"
#import "TuyaAppTheme.h"
#import "FCAlertView.h"
#import "TuyaRNDeviceListener.h"
#import "KeylessListener.h"
#import "MBProgressHUD.h"

#define kTitle  @"title"
#define kValue  @"value"
#define kAction @"action"
#define kArrow  @"arrow"
#define kSwitch @"switch"



@interface CameraSettingsViewController () <TuyaSmartCameraDPObserver, UITableViewDelegate, UITableViewDataSource>
@property (weak, nonatomic) IBOutlet UITableView                        *settinngsTableView;
@property (nonatomic, assign) BOOL                                      indicatorOn;
@property (nonatomic, assign) BOOL                                      flipOn;
@property (nonatomic, assign) BOOL                                      osdOn;
@property (nonatomic, assign) BOOL                                      privateOn;
@property (nonatomic, strong) TuyaSmartCameraNightvision                nightvisionState;
@property (nonatomic, strong) TuyaSmartCameraPIR                        pirState;
@property (nonatomic, assign) BOOL                                      motionDetectOn;
@property (nonatomic, strong) TuyaSmartCameraMotion                     motionSensitivity;
@property (nonatomic, assign) BOOL                                      decibelDetectOn;
@property (nonatomic, strong) TuyaSmartCameraDecibel                    decibelSensitivity;
@property (nonatomic, assign) TuyaSmartCameraSDCardStatus               sdCardStatus;
@property (nonatomic, assign) BOOL                                      sdRecordOn;
@property (nonatomic, strong) TuyaSmartCameraRecordMode                 recordMode;
@property (nonatomic, assign) BOOL                                      batteryLockOn;
@property (nonatomic, strong) TuyaSmartCameraPowerMode                  powerMode;
@property (nonatomic, assign) NSInteger                                 electricity;
@property (nonatomic, strong) NSArray                                   *dataSource;
@property (nonatomic, strong) TuyaSmartDevice                           *device;
@property (nonatomic, strong) UIView                                    *headerView;
@property (nonatomic, strong) NSString                                  *chimeSettings;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint                 *tableTopConstraiint;
@property (weak, nonatomic) IBOutlet UIButton                           *removeCameraButton;
@property (strong, nonatomic) UIRefreshControl                          *refreshControl;


@end

@implementation CameraSettingsViewController 

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _refreshControl = [[UIRefreshControl alloc]init];
    [_refreshControl setTintColor:[TuyaAppTheme theme].button_color];
    [_refreshControl addTarget:self action:@selector(refreshTable) forControlEvents:UIControlEventValueChanged];
    if (@available(iOS 10.0, *)) {
        self.settinngsTableView.refreshControl = _refreshControl;
    } else {
        [self.settinngsTableView addSubview:_refreshControl];
    }
}

-(void)refreshTable {
    [_refreshControl endRefreshing];
    [self getDeviceInfo];
}

- (void)viewWillAppear:(BOOL)animated {
    self.topBarView.leftItem = self.leftBackItem;

    [self.topBarView setBackgroundColor:[TuyaAppTheme theme].navbar_bg_color];
    if (@available(iOS 13, *)) {
        CGFloat topbarHeight = (self.navigationController.navigationBar.frame.size.height ?: 0.0);
        _tableTopConstraiint.constant = topbarHeight;
    }
    [self.view setBackgroundColor:[TuyaAppTheme theme].navbar_bg_color];

    [self.dpManager addObserver:self];
    [self getDeviceInfo];
    _removeCameraButton.layer.cornerRadius = _removeCameraButton.frame.size.height / 2;
    _removeCameraButton.clipsToBounds = YES;
}

- (NSString *)titleForCenterItem {
    return @"Settings";
}

- (void)removeAction {
    __weak typeof(self) weakSelf = self;
    [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    KeylessListener *listner = [KeylessListener allocWithZone:nil];
    [listner sendCameraRemoveCommandForDeviceID:self.device.deviceModel.devId callback:^(BOOL success, NSString * _Nonnull errorMessage) {
        [MBProgressHUD hideHUDForView:weakSelf.view animated:YES];
        if (success && [errorMessage isEqualToString:@"REMOVE_CAMERA_SUCCESS"]) {
            [weakSelf.navigationController popToRootViewControllerAnimated:YES];
        } else {
        }
    }];
}

- (IBAction)removeCameraButtonAction:(UIButton *)sender {
    [self removeAction];
}

- (NSString *)returnChimeTypeForValue:(NSInteger)chimeValue {
    switch (chimeValue) {
        case 0:
            return @"Not Selected";
            break;
        case 1:
            return @"Mechanical";
            break;
        case 2:
            return @"Wireless";
            break;
        case 3:
            return @"No Bells";
            break;
        default:
            return @"Not Selected";
            break;
    }
}

- (void)getDeviceInfo {
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicIndicatorDPName]) {
        self.indicatorOn = [[self.dpManager valueForDP:TuyaSmartCameraBasicIndicatorDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicFlipDPName]) {
        self.flipOn = [[self.dpManager valueForDP:TuyaSmartCameraBasicFlipDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicOSDDPName]) {
        self.osdOn = [[self.dpManager valueForDP:TuyaSmartCameraBasicOSDDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicPrivateDPName]) {
        self.privateOn = [[self.dpManager valueForDP:TuyaSmartCameraBasicPrivateDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicNightvisionDPName]) {
        self.nightvisionState = [[self.dpManager valueForDP:TuyaSmartCameraBasicNightvisionDPName] tysdk_toString];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicPIRDPName]) {
        self.pirState = [[self.dpManager valueForDP:TuyaSmartCameraBasicPIRDPName] tysdk_toString];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraMotionDetectDPName]) {
        self.motionDetectOn = [[self.dpManager valueForDP:TuyaSmartCameraMotionDetectDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraMotionSensitivityDPName]) {
        self.motionSensitivity = [[self.dpManager valueForDP:TuyaSmartCameraMotionSensitivityDPName] tysdk_toString];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraDecibelDetectDPName]) {
        self.decibelDetectOn = [[self.dpManager valueForDP:TuyaSmartCameraDecibelDetectDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraDecibelSensitivityDPName]) {
        self.decibelSensitivity = [[self.dpManager valueForDP:TuyaSmartCameraDecibelSensitivityDPName] tysdk_toString];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraSDCardStatusDPName]) {
        self.sdCardStatus = [[self.dpManager valueForDP:TuyaSmartCameraSDCardStatusDPName] tysdk_toInt];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraSDCardRecordDPName]) {
        self.sdRecordOn = [[self.dpManager valueForDP:TuyaSmartCameraSDCardRecordDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraRecordModeDPName]) {
        self.recordMode = [[self.dpManager valueForDP:TuyaSmartCameraRecordModeDPName] tysdk_toString];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraWirelessBatteryLockDPName]) {
        self.batteryLockOn = [[self.dpManager valueForDP:TuyaSmartCameraWirelessBatteryLockDPName] tysdk_toBool];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraWirelessPowerModeDPName]) {
        self.powerMode = [[self.dpManager valueForDP:TuyaSmartCameraWirelessPowerModeDPName] tysdk_toString];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraWirelessElectricityDPName]) {
        self.electricity = [[self.dpManager valueForDP:TuyaSmartCameraWirelessElectricityDPName] tysdk_toInt];
    }
    if ([self.dpManager isSupportDP:@"165"]) { // Chime Settings
        NSInteger number  = [[self.dpManager valueForDP:@"165"] tysdk_toInt];
        self.chimeSettings = [self returnChimeTypeForValue:number];
    }
    
    
    [self reloadData];
}

- (void)reloadData {
    NSMutableArray *dataSource = [NSMutableArray new];
    NSMutableArray *section0 = [NSMutableArray new];
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicIndicatorDPName]) {
        [section0 addObject:@{kTitle:NSLocalizedString(@"ipc_basic_status_indicator", @""), kValue: @(self.indicatorOn), kAction: @"indicatorAction:", kSwitch: @"1"}];
    }
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicFlipDPName]) {
        [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_basic_picture_flip", @""), kValue: @(self.flipOn), kAction: @"flipAction:", kSwitch: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicOSDDPName]) {
        [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_basic_osd_watermark", @""), kValue: @(self.osdOn), kAction: @"osdAction:", kSwitch: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicPrivateDPName]) {
        [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_basic_hibernate", @""), kValue: @(self.privateOn), kAction: @"privateAction:", kSwitch: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicNightvisionDPName]) {
        NSString *text = [self nightvisionText:self.nightvisionState];
        [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_basic_night_vision", @""), kValue: text, kAction: @"nightvisionAction", kArrow: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraBasicPIRDPName]) {
        NSString *text = [self pirText:self.pirState];
        [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_pir_switch", @""), kValue: text, kAction: @"pirAction", kArrow: @"1"}];
    }
    
    if (section0.count > 0) {
        [dataSource addObject:@{kTitle:NSLocalizedString(@"ipc_settings_page_basic_function_txt", @""), kValue: section0.copy}];
    }
    
    NSMutableArray *section1 = [NSMutableArray new];
    if ([self.dpManager isSupportDP:TuyaSmartCameraMotionDetectDPName]) {
        [section1 addObject:@{kTitle: NSLocalizedString(@"ipc_live_page_cstorage_motion_detected", @""), kValue: @(self.motionDetectOn), kAction: @"motionDetectAction:", kSwitch: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraMotionSensitivityDPName] && self.motionDetectOn) {
        NSString *text = [self motionSensitivityText:self.motionSensitivity];
        [section1 addObject:@{kTitle: NSLocalizedString(@"ipc_motion_sensitivity_settings", @""), kValue: text, kAction: @"motionSensitivityAction", kArrow: @"1"}];
    }
    if (section1.count > 0) {
        [dataSource addObject:@{kTitle: NSLocalizedString(@"ipc_live_page_cstorage_motion_detected", @""), kValue: section1.copy}];
    }
    
    NSMutableArray *section2 = [NSMutableArray new];
    if ([self.dpManager isSupportDP:TuyaSmartCameraDecibelDetectDPName]) {
        [section2 addObject:@{kTitle: NSLocalizedString(@"ipc_sound_detect_switch", @""), kValue: @(self.decibelDetectOn), kAction: @"decibelDetectAction:", kSwitch: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraDecibelSensitivityDPName] && self.decibelDetectOn) {
        NSString *text = [self decibelSensitivityText:self.decibelSensitivity];
        [section2 addObject:@{kTitle: NSLocalizedString(@"ipc_motion_sensitivity_settings", @""), kValue: text, kAction: @"decibelSensitivityAction", kArrow: @"1"}];
    }
    if (section2.count > 0) {
        [dataSource addObject:@{kTitle: NSLocalizedString(@"ipc_sound_detected_switch_settings", @""), kValue: section2.copy}];
    }
    
    NSMutableArray *section3 = [NSMutableArray new];
    if ([self.dpManager isSupportDP:TuyaSmartCameraSDCardStatusDPName]) {
        NSString *text = [self sdCardStatusText:self.sdCardStatus];
        [section3 addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_settings", @""), kValue: text, kAction: @"sdCardAction", kArrow: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraSDCardRecordDPName]) {
        [section3 addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_record_switch", @""), kValue: @(self.sdRecordOn), kAction: @"sdRecordAction:", kSwitch: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraRecordModeDPName]) {
        NSString *text = [self recordModeText:self.recordMode];
        [section3 addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_record_mode_settings", @""), kValue: text, kAction: @"recordModeAction", kArrow: @"1"}];
    }
    
    [section3 addObject:@{kTitle: @"Reset WiFi", kValue: @"", kAction: @"resetWifiAction", kArrow: @"1"}];
    
    if (section3.count > 0) {
        [dataSource addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_settings", @""), kValue: section3.copy}];
    }
    
    NSMutableArray *section4 = [NSMutableArray new];
    if ([self.dpManager isSupportDP:TuyaSmartCameraWirelessBatteryLockDPName]) {
        [section4 addObject:@{kTitle: NSLocalizedString(@"ipc_basic_batterylock", @""), kValue: @(self.batteryLockOn), kAction: @"batteryLockAction:", kSwitch: @"1"}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraWirelessPowerModeDPName]) {
        NSString *text = [self powerModeText:self.powerMode];
        [section4 addObject:@{kTitle: NSLocalizedString(@"ipc_electric_power_source", @""), kValue: text}];
    }
    
    if ([self.dpManager isSupportDP:TuyaSmartCameraWirelessElectricityDPName]) {
        NSString *text = [self electricityText];
        [section4 addObject:@{kTitle: NSLocalizedString(@"ipc_electric_percentage", @""), kValue: text}];
    }
    
    if (section4.count > 0) {
        [dataSource addObject:@{kTitle: NSLocalizedString(@"ipc_electric_title", @""), kValue: section4.copy}];
    }
    
    NSMutableArray *section5 = [NSMutableArray new];
    if ([self.dpManager isSupportDP:@"165"]) {
        [section5 addObject:@{kTitle: @"Chime Type", kValue: self.chimeSettings, kAction: @"changeChimeSettingsAction", kArrow: @"1"}];
    }
    
    if (section5.count > 0) {
        [dataSource addObject:@{kTitle: @"Bell/Chime Settings", kValue: section5.copy}];
    }

    
    self.dataSource = [dataSource copy];
    [self.settinngsTableView reloadData];
}


#pragma mark - Action Methods

- (void)indicatorAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraBasicIndicatorDPName success:^(id result) {
        weakSelf.indicatorOn = switchButton.on;
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (void)flipAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraBasicFlipDPName success:^(id result) {
        weakSelf.flipOn = switchButton.on;
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (void)osdAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraBasicOSDDPName success:^(id result) {
        weakSelf.osdOn = switchButton.on;
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (void)privateAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraBasicPrivateDPName success:^(id result) {
        weakSelf.privateOn = switchButton.on;
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (void)changeChimeSettingsAction {
    NSArray *options = @[@{kTitle: @"Mechanical",
                           kValue: @"1"},
                         @{kTitle: @"Wireless",
                           kValue: @"2"},
                         @{kTitle: @"No Bells",
                           kValue: @"3"}];
    
    __weak typeof(self) weakSelf = self;
    [self showActionSheet:options withTitle:@"Chime Type" selectedHandler:^(id result) {
        [self.dpManager setValue:result forDP:@"165" success:^(id result) {
            NSInteger Number = [result integerValue];
            weakSelf.chimeSettings = [weakSelf returnChimeTypeForValue:Number];
            [weakSelf reloadData];
        } failure:^(NSError *error) {
            
        }];
    }];
}

- (void)nightvisionAction {
    NSArray *options = @[@{kTitle: [self nightvisionText:TuyaSmartCameraNightvisionAuto],
                           kValue: TuyaSmartCameraNightvisionAuto},
                         @{kTitle: [self nightvisionText:TuyaSmartCameraNightvisionOn],
                           kValue: TuyaSmartCameraNightvisionOn},
                         @{kTitle: [self nightvisionText:TuyaSmartCameraNightvisionOff],
                           kValue: TuyaSmartCameraNightvisionOff}];
    __weak typeof(self) weakSelf = self;
    [self showActionSheet:options withTitle:@"Night Vision" selectedHandler:^(id result) {
        [self.dpManager setValue:result forDP:TuyaSmartCameraBasicNightvisionDPName success:^(id result) {
            weakSelf.nightvisionState = result;
            [weakSelf reloadData];
        } failure:^(NSError *error) {
            
        }];
    }];
}

- (void)pirAction {
    NSArray *options = @[@{kTitle: [self pirText:TuyaSmartCameraPIRStateHigh],
                           kValue: TuyaSmartCameraPIRStateHigh},
                         @{kTitle: [self pirText:TuyaSmartCameraPIRStateMedium],
                           kValue: TuyaSmartCameraPIRStateMedium},
                         @{kTitle: [self pirText:TuyaSmartCameraPIRStateLow],
                           kValue: TuyaSmartCameraPIRStateLow},
                         @{kTitle: [self pirText:TuyaSmartCameraPIRStateOff],
                           kValue: TuyaSmartCameraPIRStateOff}];
    __weak typeof(self) weakSelf = self;
    [self showActionSheet:options withTitle:@"PIR" selectedHandler:^(id result) {
        [self.dpManager setValue:result forDP:TuyaSmartCameraBasicPIRDPName success:^(id result) {
            weakSelf.pirState = result;
            [weakSelf reloadData];
        } failure:^(NSError *error) {
            
        }];
    }];
}

- (void)motionDetectAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraMotionDetectDPName success:^(id result) {
        weakSelf.motionDetectOn = switchButton.on;
        [weakSelf reloadData];
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (void)motionSensitivityAction {
    NSArray *options = @[@{kTitle: [self motionSensitivityText:TuyaSmartCameraMotionHigh],
                           kValue: TuyaSmartCameraMotionHigh},
                         @{kTitle: [self motionSensitivityText:TuyaSmartCameraMotionMedium],
                           kValue: TuyaSmartCameraMotionMedium},
                         @{kTitle: [self motionSensitivityText:TuyaSmartCameraMotionLow],
                           kValue: TuyaSmartCameraMotionLow}];
    __weak typeof(self) weakSelf = self;
    [self showActionSheet:options withTitle:@"Motion Sensitivity" selectedHandler:^(id result) {
        [self.dpManager setValue:result forDP:TuyaSmartCameraMotionSensitivityDPName success:^(id result) {
            weakSelf.motionSensitivity = result;
            [weakSelf reloadData];
        } failure:^(NSError *error) {
            
        }];
    }];
}

- (void)decibelDetectAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraDecibelDetectDPName success:^(id result) {
        weakSelf.decibelDetectOn = switchButton.on;
        [weakSelf reloadData];
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (void)decibelSensitivityAction {
    NSArray *options = @[@{kTitle: [self decibelSensitivityText:TuyaSmartCameraDecibelHigh],
                           kValue: TuyaSmartCameraDecibelHigh},
                         @{kTitle: [self decibelSensitivityText:TuyaSmartCameraDecibelLow],
                           kValue: TuyaSmartCameraDecibelLow}];
    __weak typeof(self) weakSelf = self;
    [self showActionSheet:options withTitle: @"Decibal Sensitivity" selectedHandler:^(id result) {
        [self.dpManager setValue:result forDP:TuyaSmartCameraDecibelSensitivityDPName success:^(id result) {
            weakSelf.decibelSensitivity = result;
            [weakSelf reloadData];
        } failure:^(NSError *error) {
            
        }];
    }];
}

- (void)sdCardAction {
    TuyaAppCameraSDCardViewController *vc = [TuyaAppCameraSDCardViewController new];
    vc.dpManager = self.dpManager;
    [self.navigationController pushViewController:vc animated:YES];
}

- (void)sdRecordAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraSDCardRecordDPName success:^(id result) {
        weakSelf.sdRecordOn = switchButton.on;
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (void)recordModeAction {
    NSArray *options = @[@{kTitle: [self recordModeText:TuyaSmartCameraRecordModeEvent],
                           kValue: TuyaSmartCameraRecordModeEvent},
                         @{kTitle: [self recordModeText:TuyaSmartCameraRecordModeAlways],
                           kValue: TuyaSmartCameraRecordModeAlways}];
    __weak typeof(self) weakSelf = self;
    [self showActionSheet:options withTitle: @"Recording Type" selectedHandler:^(id result) {
        
        [self.dpManager setValue:result forDP:TuyaSmartCameraRecordModeDPName success:^(id result) {
            weakSelf.recordMode = result;
            [weakSelf reloadData];
        } failure:^(NSError *error) {
            
        }];
    }];
}

- (void)resetWifiAction {
    FCAlertView *alert = [[FCAlertView alloc] init];
    [alert showAlertInView:self
                 withTitle:@"Reset WiFi"
              withSubtitle:@"Please follow the reset WiFi instructions shown earlier in the video on set up WiFi screen."
           withCustomImage:nil
       withDoneButtonTitle:nil
                andButtons:nil];
}

- (void)batteryLockAction:(UISwitch *)switchButton {
    __weak typeof(self) weakSelf = self;
    [self.dpManager setValue:@(switchButton.on) forDP:TuyaSmartCameraWirelessBatteryLockDPName success:^(id result) {
        weakSelf.batteryLockOn = switchButton.on;
    } failure:^(NSError *error) {
        [weakSelf reloadData];
    }];
}

- (NSString *)nightvisionText:(TuyaSmartCameraNightvision)state {
    if ([state isEqualToString:TuyaSmartCameraNightvisionAuto]) {
        return NSLocalizedString(@"ipc_basic_night_vision_auto", @"");
    }
    if ([state isEqualToString:TuyaSmartCameraNightvisionOn]) {
        return NSLocalizedString(@"ipc_basic_night_vision_on", @"");
    }
    return NSLocalizedString(@"ipc_basic_night_vision_off", @"");
}

- (NSString *)pirText:(TuyaSmartCameraPIR)state {
    if ([state isEqualToString:TuyaSmartCameraPIRStateLow]) {
        return NSLocalizedString(@"ipc_settings_status_low", @"");
    }
    if ([state isEqualToString:TuyaSmartCameraPIRStateMedium]) {
        return NSLocalizedString(@"ipc_settings_status_mid", @"");
    }
    if ([state isEqualToString:TuyaSmartCameraPIRStateHigh]) {
        return NSLocalizedString(@"ipc_settings_status_high", @"");
    }
    return NSLocalizedString(@"ipc_settings_status_off", @"");
}

- (NSString *)motionSensitivityText:(TuyaSmartCameraMotion)sensitivity {
    if ([sensitivity isEqualToString:TuyaSmartCameraMotionLow]) {
        return NSLocalizedString(@"ipc_motion_sensitivity_low", @"");
    }
    if ([sensitivity isEqualToString:TuyaSmartCameraMotionMedium]) {
        return NSLocalizedString(@"ipc_motion_sensitivity_mid", @"");
    }
    if ([sensitivity isEqualToString:TuyaSmartCameraMotionHigh]) {
        return NSLocalizedString(@"ipc_motion_sensitivity_high", @"");
    }
    return @"";
}

- (NSString *)decibelSensitivityText:(TuyaSmartCameraDecibel)sensitivity {
    if ([sensitivity isEqualToString:TuyaSmartCameraDecibelLow]) {
        return NSLocalizedString(@"ipc_sound_sensitivity_low", @"");
    }
    if ([sensitivity isEqualToString:TuyaSmartCameraDecibelHigh]) {
        return NSLocalizedString(@"ipc_sound_sensitivity_high", @"");
    }
    return @"";
}

- (NSString *)sdCardStatusText:(TuyaSmartCameraSDCardStatus)status {
    switch (status) {
        case TuyaSmartCameraSDCardStatusNormal:
            return NSLocalizedString(@"Normally", @"");
        case TuyaSmartCameraSDCardStatusException:
            return NSLocalizedString(@"Abnormally", @"");
        case TuyaSmartCameraSDCardStatusMemoryLow:
            return NSLocalizedString(@"Insufficient capacity", @"");
        case TuyaSmartCameraSDCardStatusFormatting:
            return NSLocalizedString(@"ipc_status_sdcard_format", @"");
        default:
            return NSLocalizedString(@"pps_no_sdcard", @"");
    }
}

- (NSString *)recordModeText:(TuyaSmartCameraRecordMode)mode {
    if ([mode isEqualToString:TuyaSmartCameraRecordModeEvent]) {
        return NSLocalizedString(@"ipc_sdcard_record_mode_event", @"");
    }
    return NSLocalizedString(@"ipc_sdcard_record_mode_ctns", @"");
}

- (NSString *)powerModeText:(TuyaSmartCameraPowerMode)mode {
    if ([mode isEqualToString:TuyaSmartCameraPowerModePlug]) {
        return NSLocalizedString(@"ipc_electric_power_source_wire", @"");
    }
    return NSLocalizedString(@"ipc_electric_power_source_batt", @"");
}

- (NSString *)electricityText {
    return [NSString stringWithFormat:@"%@%%", @(self.electricity)];
}

- (void)showActionSheet:(NSArray *)options withTitle:(NSString *)title selectedHandler:(void(^)(id result))handler {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:title message:@"" preferredStyle:UIAlertControllerStyleActionSheet];
    [options enumerateObjectsUsingBlock:^(NSDictionary *obj, NSUInteger idx, BOOL * _Nonnull stop) {
        UIAlertAction *action = [UIAlertAction actionWithTitle:[obj objectForKey:kTitle] style:UIAlertActionStyleDefault handler:^(UIAlertAction * action) {
            if (handler) {
                handler([obj objectForKey:kValue]);
            }
        }];
        [alert addAction:action];
    }];
    [self presentViewController:alert animated:YES completion:nil];
}

#pragma mark - dpmanagerobserver

- (void)cameraDPDidUpdate:(TuyaSmartCameraDPManager *)manager dps:(NSDictionary *)dpsData {
    if ([dpsData objectForKey:TuyaSmartCameraWirelessElectricityDPName]) {
        self.electricity = [[dpsData objectForKey:TuyaSmartCameraWirelessElectricityDPName] integerValue];
        [self reloadData];
    }
}

#pragma mark - Accessor method
- (TuyaSmartDevice *)device {
    if (!_device) {
        _device = [TuyaSmartDevice deviceWithDeviceId:self.devId];
    }
    return _device;
}

#pragma mark - Table View Delegate Methods

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.dataSource.count;
}

//- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
//    return [[self.dataSource objectAtIndex:section] objectForKey:kTitle];
//}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section {
    _headerView = [[UIView alloc] initWithFrame: CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, 40)];
    UIView *paddinngView = [[UIView alloc] initWithFrame: CGRectMake(0, _headerView.frame.origin.y, 15, 40)];
    [_headerView setBackgroundColor:[UIColor whiteColor]];
    UILabel *headerLabel = [[UILabel alloc] initWithFrame:CGRectMake(15, _headerView.frame.origin.y, _headerView.frame.size.width, _headerView.frame.size.height)];
    UIFont *font = [UIFont fontWithName:@"Quicksand-Medium" size:16.0];
    NSDictionary *attribs = @{ NSForegroundColorAttributeName: [UIColor colorWithRed:63.0/255.0 green:77.0/255.0 blue:89.0/255.0 alpha:1.0],
                               NSFontAttributeName: font
                            };
    NSMutableAttributedString *attributedText = [[NSMutableAttributedString alloc] initWithString:[[self.dataSource objectAtIndex:section] objectForKey:kTitle] attributes:attribs];
    [headerLabel setAttributedText: attributedText];
    [_headerView addSubview:paddinngView];
    [_headerView addSubview:headerLabel];
    
    return _headerView;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [[[self.dataSource objectAtIndex:section] objectForKey:kValue] count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 40;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 60;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSDictionary *data = [[[self.dataSource objectAtIndex:indexPath.section] valueForKey:kValue] objectAtIndex:indexPath.row];
    if ([data objectForKey:kSwitch]) {
        CameraSettingsTableViewCell *cell = (CameraSettingsTableViewCell *)[tableView dequeueReusableCellWithIdentifier:@"SettingSwitchCell" forIndexPath:indexPath];
        BOOL value = [[data objectForKey:kValue] boolValue];
        SEL action = NSSelectorFromString([data objectForKey:kAction]);
        cell.settingSwitch.on = value;
        cell.settingsLabel.text = [data objectForKey:kTitle];
        cell.settingSwitch.onTintColor = [TuyaAppTheme theme].button_color;
        [cell.settingSwitch addTarget:self action:action forControlEvents:UIControlEventValueChanged];
        
        return cell;
    } else {
        CameraSettingsTableViewCell *cell = (CameraSettingsTableViewCell *)[tableView dequeueReusableCellWithIdentifier:@"SettingArrowCell" forIndexPath:indexPath];
        cell.settingArrowLabel.text = [data objectForKey:kTitle];
        [cell.settingArrowButton setTitle:[data objectForKey:kValue] forState:UIControlStateNormal];
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    NSDictionary *data = [[[self.dataSource objectAtIndex:indexPath.section] objectForKey:kValue] objectAtIndex:indexPath.row];
    if (![data objectForKey:kSwitch]) {
        NSString *action = [data objectForKey:kAction];
        if (action) {
            SEL selector = NSSelectorFromString(action);
            [self performSelector:selector withObject:nil afterDelay:0];
        }
    }
}


@end
