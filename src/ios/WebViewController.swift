import UIKit
import WebKit


class WebViewController: UIViewController, WKUIDelegate, WKNavigationDelegate {

    var webView: WKWebView!
    var url: String?
    var param: String?
    var cordovaPlugin: NiceId?

    init(cordovaPlugin: NiceId?, url: String?, param: String?) {
        super.init(nibName: nil, bundle: nil)
        self.cordovaPlugin = cordovaPlugin
        self.url = url
        self.param = param
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func loadView() {
        
        super.loadView()
        self.webView = WKWebView(frame: self.view.frame)
        if (self.url?.isEmpty==false) {
            let url = URL(string: self.url!)
            let request = URLRequest(url: url!)
            self.webView.configuration.preferences.javaScriptEnabled = true
            self.webView.uiDelegate = self
            self.webView.navigationDelegate = self
            self.view = self.webView
            webView.load(request)
        }
    
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        // UIApplication.shared.isNetworkActivityIndicatorVisible = false
        let urlStr: String? = webView.url?.path
        if (urlStr?.isEmpty==false) {
            print("url " + urlStr!)
            if (urlStr!.contains("checkplus_ok")) {
                self.webView?.evaluateJavaScript("setTimeout( function() { call() }, 10);", completionHandler: { (result, error) in
                    if error == nil {
                        let resultStr =  result as? String ?? ""
                        print("result=", resultStr, ".")
                        self.cordovaPlugin?.getResult(data: resultStr)
                        self.dismiss(animated: true)
                    } else {
                        print("evaluateJavascript error " + error.debugDescription)
                    }
                })
           }
        }
    }
    
}
