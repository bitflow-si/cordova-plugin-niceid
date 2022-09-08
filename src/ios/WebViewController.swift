import UIKit
import WebKit


class WebViewController: UIViewController,WKUIDelegate,WKNavigationDelegate {

    var webView: WKWebView!
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        let url = URL(string: "https://naver.com")
        let request = URLRequest(url: url!)
        webView.configuration.preferences.javaScriptEnabled = true
        webView.load(request)
        
    }

    override func loadView() {
        
        super.loadView()
        webView = WKWebView(frame: self.view.frame)
        webView.uiDelegate = self
        webView.navigationDelegate = self
        self.view = self.webView
    
    }

}
