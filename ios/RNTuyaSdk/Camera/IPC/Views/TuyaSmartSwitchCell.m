//
//  TuyaSmartSwitchCell.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "TuyaSmartSwitchCell.h"

@implementation TuyaSmartSwitchCell

- (void)layoutSubviews {
    [super layoutSubviews];
    self.switchButton.center = CGPointMake(self.frame.size.width - 40, self.frame.size.height / 2);
}

- (void)setValueChangedTarget:(id)target selector:(SEL)selector value:(BOOL)value {
    self.switchButton.on = value;
    [self.switchButton addTarget:target action:selector forControlEvents:UIControlEventValueChanged];
}

- (UISwitch *)switchButton {
    if (!_switchButton) {
        _switchButton = [[UISwitch alloc] init];
        [self.contentView addSubview:_switchButton];
    }
    return _switchButton;
}

@end
