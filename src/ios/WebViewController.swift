import UIKit
import WebKit
import Foundation

class WebViewController: UIViewController,WKNavigationDelegate, WKUIDelegate {
    
    var webView: WKWebView!
    var command: CDVInvokedUrlCommand
    var url: NSString
    var commandDelegate: CDVCommandDelegate
    
    init(command: CDVInvokedUrlCommand, delegate: CDVCommandDelegate) {
        self.command = command
        self.url = command.argument(at: 0)[1]
        self.commandDelegate = delegate
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setPreferenceWithId(_ key: String, value: Any) {
        UserDefaults.standard.set(value, forKey: key)
        UserDefaults.standard.synchronize()
    }
    
    func getPreferenceWithId(_ key: String) -> Any? {
        return UserDefaults.standard.value(forKey: key)
    }
    
    override func loadView() {
        let webConfiguration = WKWebViewConfiguration()
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        //        webView.navigationDelegate = self
        //        webView.uiDelegate = self
        //        webView.configuration.preferences.javaScriptEnabled = true
        view = webView
    }
    
    func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        let requestURL = navigationAction.request.url?.absoluteString ?? ""
        print("Request URL: \(requestURL)")
        
        webView.evaluateJavaScript("document.cookie") { (result, error) in
            if let resVal = result as? String {
                print("saveCookie : " + resVal)
                
                let dataComponents = resVal.components(separatedBy: "; ")
                for dataComponent in dataComponents {
                    let keyValue = dataComponent.components(separatedBy: "=")
                    if(keyValue.count == 2){
                        let key = keyValue[0]
                        let value = keyValue[1]
                        self.setPreferenceWithId(key, value: value)
                        print(key, value)
                    }
                }
            }
        }
        
        decisionHandler(.allow)
    }
    
    func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        print(webView.url?.absoluteString ?? "", "didFail")
        print("Failed to load the URL with error: \(error.localizedDescription)")
    }
    
    func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
        print(webView.url?.absoluteString ?? "", "didFailProvisionNavigation")
        print("Failed to load the URL with error: \(error.localizedDescription)")
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        let presentUrl = webView.url?.absoluteString as? String ?? ""
        print(presentUrl, "didFinish")
    }
    
    func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
        let presentUrl = webView.url?.absoluteString as? String ?? ""
        print(presentUrl, "didStartProvisionalNavigation")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        webView = WKWebView(frame: .zero)
        
        webView.navigationDelegate = self
        webView.uiDelegate = self
        webView.translatesAutoresizingMaskIntoConstraints = false
        
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
        
        webView.translatesAutoresizingMaskIntoConstraints = false
        
        NSLayoutConstraint.activate([
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            webView.topAnchor.constraint(equalTo: view.topAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        let request = URLRequest(URL(self.url))
        webView.load(request)
    }
    
}
