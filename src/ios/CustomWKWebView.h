//
//  CustomWKWebView.h
//  GuideApp
//
//  Created by iOSDev on 2018. 8. 29..
//  Copyright © 2018년 Nice. All rights reserved.
//


#import <UIKit/UIKit.h>
#import <WebKit/WKUIDelegate.h>
#import <WebKit/WKNavigationDelegate.h>

//웹과 뷰사이에 필요한 동작
typedef NS_ENUM(NSInteger, WKWebViewAction) {
    WKWebViewActionDidFinishNavigation = 0,
    WKWebViewActionDidStartProvisionalNavigation,
    WKWebViewActionDidFailNavigation
};

typedef void(^WKWebViewActionHandler)(WKWebViewAction action, id result);

@class WKWebView;
@class WKWebViewConfiguration;

@interface CustomWKWebView : UIView<WKUIDelegate, WKNavigationDelegate>

- (void)loadUrl:(NSString *)url;
- (WKWebView *)getWKWebView;
- (void)setWKWebViewAction:(WKWebViewActionHandler)actionHandler;

@end
