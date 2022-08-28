import UIKit
import WebKit


class WebViewController: UIViewController,WKUIDelegate,WKNavigationDelegate {

    var webView: WKWebView!
    var url: String?
    var param: String?

    init(url: String?, param: String?) {
        super.init(nibName: nil, bundle: nil)
        self.url = url
        self.param = param
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        if (self.url?.isEmpty==false) {
            let url = URL(string: self.url!)
            let request = URLRequest(url: url!)
            webView.configuration.preferences.javaScriptEnabled = true
            webView.load(request)
        }
    }

    override func loadView() {
        
        super.loadView()
        webView = WKWebView(frame: self.view.frame)
        webView.uiDelegate = self
        webView.navigationDelegate = self
        self.view = self.webView
    
    }

}
