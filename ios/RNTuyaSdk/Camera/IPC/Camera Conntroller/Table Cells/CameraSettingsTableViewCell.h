//
//  CameraSettingsTableViewCell.h
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 16/02/21.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface CameraSettingsTableViewCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UISwitch *settingSwitch;
@property (weak, nonatomic) IBOutlet UILabel *settingsLabel;
@property (weak, nonatomic) IBOutlet UIView *settingSepratorView;

@property (weak, nonatomic) IBOutlet UILabel *settingArrowLabel;
@property (weak, nonatomic) IBOutlet UIButton *settingArrowButton;
@property (weak, nonatomic) IBOutlet UIView *settingArrowSepratorView;

@end

NS_ASSUME_NONNULL_END
