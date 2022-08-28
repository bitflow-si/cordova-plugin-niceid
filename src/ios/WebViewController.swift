import UIKit
import WebKit


class WebViewController: UIViewController, WKUIDelegate, WKNavigationDelegate, WKScriptMessageHandler {

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
        
        
        let webConfiguration = WKWebViewConfiguration()
        let userScript = WKUserScript(source: "call()", injectionTime: .atDocumentEnd, forMainFrameOnly: true)
        let contentController = WKUserContentController()
        contentController.addUserScript(userScript)
        contentController.add(self, name: "callbackHandler")

        webConfiguration.userContentController = contentController
        self.webView = WKWebView(frame: self.view.frame, configuration: webConfiguration)
        
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
    
    @available(iOS 8.0, *)
    public func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Swift.Void){
        let alert = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        let otherAction = UIAlertAction(title: "OK", style: .default, handler: {action in completionHandler()})
        alert.addAction(otherAction)
        self.present(alert, animated: true, completion: nil)
    }

    // JS -> Native CALL
    @available(iOS 8.0, *)
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage){
        if(message.name == "callbackHandler"){
            let resultStr =  message.body  as? String ?? ""
            self.cordovaPlugin?.getResult(data: resultStr)
            self.dismiss(animated: true)
        }
    }
    
}
