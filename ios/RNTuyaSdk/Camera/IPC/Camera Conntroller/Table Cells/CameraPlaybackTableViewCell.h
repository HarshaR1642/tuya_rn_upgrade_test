//
//  CameraPlaybackTableViewCell.h
//  RNTuyaSdk
//
//  Created by Roshan Bisht on 15/02/21.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface CameraPlaybackTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UIView *playbackMessageView;
@property (weak, nonatomic) IBOutlet UILabel *playbackTitleLabel;
@property (weak, nonatomic) IBOutlet UILabel *playbackMessageLabel;
@property (weak, nonatomic) IBOutlet UIImageView *playbackMessageImageView;
@end

NS_ASSUME_NONNULL_END
