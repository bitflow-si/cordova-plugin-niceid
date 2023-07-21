//
//  CustomWKWebView.m
//  GuideApp
//
//  Created by iOSDev on 2018. 8. 29..
//  Copyright © 2018년 Nice. All rights reserved.
//

#import "CustomWKWebView.h"
#import "WKWebViewGuide.h"

#import <WebKit/WebKit.h>
#import <AVFoundation/AVFoundation.h>


@interface CustomWKWebView ()

@property (strong, nonatomic) WKWebView *wkWebView;

@property (copy, nonatomic) WKWebViewActionHandler actionHandler;


@end

@implementation CustomWKWebView

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self initWkWebview:self.frame configuration:nil];
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self initWkWebview:frame configuration:nil];
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame configuration:(WKWebViewConfiguration *)configuration {
    self = [super initWithFrame:frame];
    if (self) {
        [self initWkWebview:frame configuration:configuration];
    }
    return self;
}

- (void)initWkWebview:(CGRect)frame configuration:(WKWebViewConfiguration *)configuration {
    self.wkWebView = [[WKWebView alloc] initWithFrame:frame configuration:configuration == nil ? [self getWKWebViewConfigration] : configuration];
    
    self.wkWebView.translatesAutoresizingMaskIntoConstraints = false;
    self.wkWebView.scrollView.bounces = true;
    self.wkWebView.scrollView.showsHorizontalScrollIndicator = false;
    self.wkWebView.navigationDelegate = self;
    self.wkWebView.UIDelegate= self;
    self.wkWebView.scrollView.scrollsToTop = true;
    
    self.wkWebView.backgroundColor = [UIColor whiteColor];
    /*
    //적용
    [self removeInputAccessoryViewFromWKWebView:self.wkWebView];
    */
    [self addSubview:self.wkWebView];
    
    //set autolayout
    [self addWKWebViewConstraint:NSLayoutAttributeTop];
    [self addWKWebViewConstraint:NSLayoutAttributeLeading];
    [self addWKWebViewConstraint:NSLayoutAttributeTrailing];
    [self addWKWebViewConstraint:NSLayoutAttributeBottom];
    
   
}


- (void)addWKWebViewConstraint:(NSLayoutAttribute)attribute {
    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.wkWebView attribute:attribute relatedBy:NSLayoutRelationEqual toItem:self attribute:attribute multiplier:1.0 constant:0.0]];
}

- (void)loadUrl:(NSString *)url {
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:url] cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:20.0f];
    [request setHTTPMethod:@"POST"];
    
    [self.wkWebView loadRequest:request];
}

//WKWebView 설정. 개발업체에 맞게 개발자가 설정
- (WKWebViewConfiguration *)getWKWebViewConfigration {
    WKWebViewConfiguration  *wkWebViewConfiguration =  [[WKWebViewConfiguration alloc] init];
    WKUserContentController *kuserContentController = [[WKUserContentController alloc] init];
    WKPreferences           *kwebviewPreference     = [[WKPreferences alloc] init];
    
    //Message 핸들러 설정
    //[kuserContentController addScriptMessageHandler:self.scriptMessageHandler name:@"bridge"];
    wkWebViewConfiguration.userContentController = kuserContentController;
    
    // 메모리에서 랜더링 후 보여줌 Defalt = false
    // true 일경우 랜더링 시간동안 Black 스크린이 나옴
    wkWebViewConfiguration.suppressesIncrementalRendering = false;
    
    // 기본값 : Dynamic (텍스트 선택시 정밀도 설정)
    wkWebViewConfiguration.selectionGranularity = WKSelectionGranularityDynamic;
    
    // 기본값 false : HTML5 Video 형태로 플레이
    // true  : native full-screen play
    wkWebViewConfiguration.allowsInlineMediaPlayback = false;
    
    if (@available(iOS 9.0, *)) {
        // whether AirPlay is allowed.
        wkWebViewConfiguration.allowsAirPlayForMediaPlayback = false;
        
        // 기본값 : true;
        // whether HTML5 videos can play picture-in-picture.
        wkWebViewConfiguration.allowsPictureInPictureMediaPlayback = true;
        
        //LocalStorage 사용하도록 설정
        wkWebViewConfiguration.websiteDataStore = [WKWebsiteDataStore defaultDataStore];
        
        if (@available(iOS 10.0, *)) {
            // 기본값 : true
            // true : 사용자가 시작 , false : 자동시작
            wkWebViewConfiguration.mediaTypesRequiringUserActionForPlayback = true;
        }
        
    }
    
    // WKPreference 셋팅
    kwebviewPreference.minimumFontSize = 0;                                   // 기본값 = 0
    kwebviewPreference.javaScriptCanOpenWindowsAutomatically = true;         // 기본값 = false
    kwebviewPreference.javaScriptEnabled = true;                              // 기본값 = true
    
    wkWebViewConfiguration.preferences = kwebviewPreference;
    
    return wkWebViewConfiguration;
}

- (void)setWKWebViewAction:(WKWebViewActionHandler)actionHandler {
    if (actionHandler != nil)
        self.actionHandler = actionHandler;
}


- (WKWebView *)getWKWebView {
    return self.wkWebView;
}

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation{
    if (self.actionHandler != nil)
        self.actionHandler(WKWebViewActionDidStartProvisionalNavigation, nil);
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation{
    if (self.actionHandler != nil)
        self.actionHandler(WKWebViewActionDidFinishNavigation, nil);
    
}

- (void)webView:(WKWebView *)webView didFailNavigation:(WKNavigation *)navigation withError:(NSError *)error {
    //DLog(@"didFailNavigation error - %@", [error description]);
    if (self.actionHandler != nil)
        self.actionHandler(WKWebViewActionDidFailNavigation, error);

}

- (void)webView:(WKWebView *)webView didFailProvisionalNavigation:(WKNavigation *)navigation withError:(NSError *)error {
    //DLog(@"didFailProvisionalNavigation error - %@", [error description]);
    if (self.actionHandler != nil)
        self.actionHandler(WKWebViewActionDidFailNavigation, error);
   
}

- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler{
    NSString *urlString = navigationAction.request.URL.absoluteString;
    
	//앱설치 링크로 진입시 별도 처리(https://itunes.apple.com/kr/app/id~~~~~~)
    if ([urlString hasPrefix:@"https://itunes.apple.com"]) {
        //스토어 연결(OS에서 처리)
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
           
		//웹뷰 내 페이지 이동 안하도록 설정(PolicyCancel)
        decisionHandler(WKNavigationActionPolicyCancel);
        return;
        
    } else if ([urlString hasPrefix:@"tauthlink"] || [urlString hasPrefix:@"ktauthexternalcall"] 
		|| [urlString hasPrefix:@"upluscorporation"]  || [urlString hasPrefix:@"niceipin2"]) {
        
		//외부 앱 Scheme로 URL이 시작되는 경우
		//tauthLink(SKT PASS 인증앱)
		//ktauthexternalcall(KT PASS 인증앱)
		//upluscorporation(LGU+ PASS 인증앱)
		//회원사에서 추가로 연동하고 싶은 앱스키마가 있다면 or 조건에 추가 해주세요. (예시 : niceipin2 )

		//앱 실행
		[[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
		
		//웹뷰 내 페이지 이동 안하도록 설정(PolicyCancel)
		decisionHandler(WKNavigationActionPolicyCancel);
		return;        
    }
    
	
	//일반 웹 페이지 링크 처리(PolicyAllow)
    decisionHandler(WKNavigationActionPolicyAllow);
    return;
    
}

- (void)webView:(WKWebView *)webView decidePolicyForNavigationResponse:(WKNavigationResponse *)navigationResponse decisionHandler:(void (^)(WKNavigationResponsePolicy))decisionHandler {
    decisionHandler(WKNavigationResponsePolicyAllow);
}



@end
