//
//  TuyaAppCellViewItem.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "TuyaAppCellViewItem.h"

@implementation TuyaAppCellViewItem

+ (TuyaAppCellViewItem *)cellItemWithTitle:(NSString *)title image:(UIImage *)image {
    TuyaAppCellViewItem *cellitem = [[TuyaAppCellViewItem alloc] init];
    cellitem.title = title;
    cellitem.image = image;
    return cellitem;
}

+ (TuyaAppCellViewItem *)cellItemWithArrowImage:(NSString *)title {
    TuyaAppCellViewItem *cellitem = [[TuyaAppCellViewItem alloc] init];
    cellitem.title = title;
    cellitem.image = [UIImage imageNamed:@"tp_list_arrow_goto"];
    return cellitem;
}

@end
