//
//  CameraMessageTableViewCell.h
//  Base64
//
//  Created by Roshan Bisht on 12/02/21.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface CameraMessageTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *messageDateLabel;
@property (weak, nonatomic) IBOutlet UILabel *messageTitleLabel;
@property (weak, nonatomic) IBOutlet UILabel *messageLabel;
@property (weak, nonatomic) IBOutlet UIImageView *messageUserImageView;

@end

NS_ASSUME_NONNULL_END
