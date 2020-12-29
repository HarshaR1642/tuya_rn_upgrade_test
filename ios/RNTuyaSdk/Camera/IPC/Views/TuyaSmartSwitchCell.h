//
//  TuyaSmartSwitchCell.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <UIKit/UIKit.h>

@interface TuyaSmartSwitchCell : UITableViewCell

@property (nonatomic, strong) UISwitch *switchButton;

- (void)setValueChangedTarget:(id)target selector:(SEL)selector value:(BOOL)value;

@end
