//
//  TuyaSmartCameraService.h
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface TuyaSmartCameraService : NSObject

+ (instancetype)sharedService;

- (NSInteger)definitionForCamera:(NSString *)devId;

- (void)setDefinition:(NSInteger)definition forCamera:(NSString *)devId;

- (NSInteger)audioModeForCamera:(NSString *)devId;

- (void)setAudioMode:(NSInteger)mode forCamera:(NSString *)devId;
    
- (BOOL)couldChangeAudioMode:(NSString *)devId;

- (void)setCouldChangedAudioMode:(BOOL)couldChange forCamera:(NSString *)devId;

- (void)observeDoorbellCall:(void(^)(NSString *devId, NSString *type))callback;

- (NSString *)thumbnailDirectoryForDevice:(NSString *)devId;

- (void)removeAllThumbnailsForDevice:(NSString *)devId;

@end

NS_ASSUME_NONNULL_END
