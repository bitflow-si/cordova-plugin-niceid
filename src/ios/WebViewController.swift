import UIKit
import WebKit

class WebViewController: UIViewController,WKNavigationDelegate, WKUIDelegate {
    
    var webView: WKWebView!
    var command: CDVInvokedUrlCommand
    var commandDelegate: CDVCommandDelegate
    
    init(command: CDVInvokedUrlCommand, delegate: CDVCommandDelegate) {
        self.command = command
        self.commandDelegate = delegate
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func loadView() {
        let webConfiguration = WKWebViewConfiguration()
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        
        view = webView
    }
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationResponse: WKNavigationResponse, decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void) {
        print(webView.url?.absoluteString ?? "", "decidePolicyFor")
        
        // 응답 내용을 가져와서 출력
//        if let response = navigationResponse.response as? HTTPURLResponse {
//            print("Response: \(response)")
//        }
        
        // 기본적으로 응답을 허용합니다.
        decisionHandler(.allow)
    }

    
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        let requestURL = navigationAction.request.url?.absoluteString ?? ""
        print(requestURL, "WKNavigationAction")
        
        if(requestURL.hasPrefix("tauthlink") || requestURL.hasPrefix("ktauthexternalcall") || requestURL.hasPrefix("upluscorporation") || requestURL.hasPrefix("niceipin2")) {
            decisionHandler(.cancel)
            return
        }
        
        decisionHandler(.allow)
        return
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        webView = WKWebView(frame: .zero)
        
        webView.translatesAutoresizingMaskIntoConstraints = false
        webView.scrollView.bounces = true
        webView.scrollView.showsHorizontalScrollIndicator = false
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.scrollView.scrollsToTop = true
        webView.configuration.suppressesIncrementalRendering = false
        webView.configuration.selectionGranularity = .dynamic
        webView.configuration.allowsInlineMediaPlayback = false
        webView.configuration.allowsAirPlayForMediaPlayback = false
        webView.configuration.allowsPictureInPictureMediaPlayback = true
        webView.configuration.websiteDataStore = .default()
        webView.configuration.mediaTypesRequiringUserActionForPlayback = .all
        
        webView.configuration.preferences.minimumFontSize = 0
        webView.configuration.preferences.javaScriptCanOpenWindowsAutomatically = true
        webView.configuration.preferences.javaScriptEnabled = true
        
        view.addSubview(webView)
        
        NSLayoutConstraint.activate([
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            webView.topAnchor.constraint(equalTo: view.topAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        if let url = URL(string: "https://test2-apihealthpilot.snuh.org/view/checkplus"){
            let request = URLRequest(url: url)
            webView.load(request)
        }
    }
    
    
}
