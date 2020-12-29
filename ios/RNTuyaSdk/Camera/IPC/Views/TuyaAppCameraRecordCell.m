//
//  TuyaAppCameraRecordCell.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "TuyaAppCameraRecordCell.h"
#import <TuyaSmartCameraBase/TuyaSmartCameraBase.h>

@implementation TuyaAppCameraRecordCell

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        _iconImageView  = [[UIImageView alloc] initWithFrame:CGRectMake(20, 6, 96, 54)];
        _startTimeLabel = [[UILabel alloc] initWithFrame:CGRectMake(15, 14, self.contentView.frame.size.width-30, 22)];
        _startTimeLabel.textColor = [UIColor blackColor];
    
        _durationLabel  = [[UILabel alloc] initWithFrame:CGRectMake(15, 38, self.contentView.frame.size.width-30, 20)];
        _durationLabel.textColor = [UIColor blackColor];
        
        CGFloat screenWidth = [UIScreen mainScreen].bounds.size.width;
        _typeLabel = [[UILabel alloc] initWithFrame:CGRectMake(screenWidth - 90, 0, 80, 72)];
        _typeLabel.textAlignment = NSTextAlignmentRight;
        
        //[self addSubview:_iconImageView];
        [self addSubview:_startTimeLabel];
        [self addSubview:_durationLabel];
        //[self addSubview:_typeLabel];
    }
    return self;
}

- (void)prepareForReuse {
    [super prepareForReuse];
    self.imageView.image = nil;
}

@end
