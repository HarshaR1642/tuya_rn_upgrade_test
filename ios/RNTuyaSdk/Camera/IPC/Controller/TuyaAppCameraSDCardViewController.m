//
//  TuyaSmartSDCardViewController.m
//  TuyaSDK
//
//  Created by Nagaraj Balan on 06/12/20.
//

#import "TuyaAppCameraSDCardViewController.h"
#import "TuyaAppTheme.h"
#import "FCAlertView.h"

#define kTitle  @"title"
#define kValue  @"value"

@interface TuyaAppCameraSDCardViewController ()<TuyaSmartCameraDPObserver, UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, assign) NSInteger total;
@property (nonatomic, assign) NSInteger used;
@property (nonatomic, assign) NSInteger left;

@property (nonatomic, strong) NSArray *dataSource;
@property (nonatomic, strong) UIButton *formatButton;

@property (nonatomic, strong) UITableView *tableView;

@end

@implementation TuyaAppCameraSDCardViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.view setBackgroundColor:[TuyaAppTheme theme].view_bg_color];
    [self.tableView setBackgroundColor:[TuyaAppTheme theme].view_bg_color];
    [self.topBarView setBackgroundColor:[TuyaAppTheme theme].navbar_bg_color];
    [self reloadTable];
    
    [self.dpManager addObserver:self];
    self.topBarView.leftItem = self.leftBackItem;
}

- (NSString *)titleForCenterItem {
    return NSLocalizedString(@"sd_card", @"");
}

- (void)reloadTable {
    __weak typeof(self) weakSelf = self;
    [self.dpManager valueForDP:TuyaSmartCameraSDCardStorageDPName success:^(id result) {
        NSArray *components = [result componentsSeparatedByString:@"|"];
        if (components.count < 3) {
            return;
        }
        weakSelf.total = [[components firstObject] integerValue];
        weakSelf.used = [[components objectAtIndex:1] integerValue];
        weakSelf.left = [[components lastObject] integerValue];
        [weakSelf reloadData];
    } failure:^(NSError *error) {
        
    }];
}

- (void)formatAction {
    __weak typeof(self) weakSelf = self;
    FCAlertView *alert = [[FCAlertView alloc] init];
    [alert showAlertInView:self
                 withTitle:NSLocalizedString(@"format", @"")
              withSubtitle:NSLocalizedString(@"format_instruction", @"")
           withCustomImage:nil
       withDoneButtonTitle:NSLocalizedString(@"format_confirm", @"")
                andButtons:nil];
    
    [alert addButton:NSLocalizedString(@"cancel", @"") withActionBlock:nil];
    [alert doneActionBlock:^{
        self.formatButton.enabled = NO;
        [self.dpManager setValue:@(YES) forDP:TuyaSmartCameraSDCardFormatDPName success:^(id result) {
            [weakSelf reloadTable];
            weakSelf.formatButton.enabled = YES;
        } failure:^(NSError *error) {
            weakSelf.formatButton.enabled = YES;
        }];
    }];
    
}

- (void)reloadData {
    NSMutableArray *dataSource = [NSMutableArray new];
    NSMutableArray *section0 = [NSMutableArray new];
    NSString *totalText = [NSString stringWithFormat:@"%.1fG", self.total / 1024.0 / 1024.0];
    NSString *usedText = [NSString stringWithFormat:@"%.1fG", self.used / 1024.0 / 1024.0];
    NSString *leftText = [NSString stringWithFormat:@"%.1fG", self.left / 1024.0 / 1024.0];
    [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_capacity_total", @""), kValue: totalText}];
    [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_capacity_used", @""), kValue: usedText}];
    [section0 addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_capacity_residue", @""), kValue: leftText}];
    
    [dataSource addObject:@{kTitle: NSLocalizedString(@"ipc_sdcard_capacity", @""), kValue: section0.copy}];
    self.dataSource = [dataSource copy];
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.dataSource.count;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [[[self.dataSource objectAtIndex:section] objectForKey:kValue] count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    NSDictionary *data = [[[self.dataSource objectAtIndex:indexPath.section] objectForKey:kValue] objectAtIndex:indexPath.row];
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell"];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:@"cell"];
    }
    
    UIFont *font = [UIFont fontWithName:@"Quicksand-Medium" size:16.0];
    NSDictionary *attribs = @{ NSForegroundColorAttributeName: [UIColor colorWithRed:63.0/255.0 green:77.0/255.0 blue:89.0/255.0 alpha:1.0],
                               NSFontAttributeName: font
                            };
    NSMutableAttributedString *attributedText1 = [[NSMutableAttributedString alloc] initWithString:[data objectForKey:kTitle] attributes:attribs];
    NSMutableAttributedString *attributedText2 = [[NSMutableAttributedString alloc] initWithString:[data objectForKey:kValue] attributes:attribs];
    [cell.textLabel setAttributedText: attributedText1];
    [cell.detailTextLabel setAttributedText:attributedText2];
    cell.backgroundColor = [UIColor whiteColor];
    return cell;
}
- (UIView *)tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section {
    UIView *footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.tableView.frame.size.width, 150)];
    UIButton *formatButton = [[UIButton alloc] initWithFrame:CGRectMake(self.tableView.frame.size.width / 2 - 100, 50, 200, 50)];
    [formatButton addTarget:self action:@selector(formatAction) forControlEvents:UIControlEventTouchUpInside];
    [formatButton setTitle:NSLocalizedString(@"ipc_sdcard_format", @"") forState:UIControlStateNormal];
    [formatButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [formatButton setBackgroundColor:[UIColor colorWithRed:254.0/255.0 green:41.0/255.0 blue:92.0/255.0 alpha:1.0]];
    formatButton.layer.cornerRadius = 25.0;
    formatButton.clipsToBounds = YES;
    self.formatButton = formatButton;
    [footerView addSubview:formatButton];
    return footerView;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 100;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    return [[self.dataSource objectAtIndex:section] objectForKey:kTitle];
}

- (void)cameraDPDidUpdate:(TuyaSmartCameraDPManager *)manager dps:(NSDictionary *)dpsData {
    if ([dpsData objectForKey:TuyaSmartCameraSDCardFormatStateDPName]) {
        NSInteger progress = [[dpsData objectForKey:TuyaSmartCameraSDCardFormatStateDPName] intValue];
        if (progress == 100) {
            self.formatButton.enabled = YES;
            NSLog(@"&&&& sd card format success");
        }
        if (progress < 0) {
            self.formatButton.enabled = YES;
            NSLog(@"&&&& sd card format failure");
        }
        NSLog(@"&&&& sd card formatting progress: %@%%", [dpsData objectForKey:TuyaSmartCameraSDCardFormatStateDPName]);
    }
}

- (UITableView *)tableView {
    if (!_tableView) {
        _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, APP_TOP_BAR_HEIGHT, APP_SCREEN_WIDTH, APP_SCREEN_HEIGHT - APP_TOP_BAR_HEIGHT) style:UITableViewStyleGrouped];
        _tableView.delegate = self;
        _tableView.dataSource = self;
        [self.view addSubview:_tableView];
    }
    return _tableView;
}

@end
