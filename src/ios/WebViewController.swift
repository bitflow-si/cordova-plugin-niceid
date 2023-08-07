import UIKit
import WebKit
import Foundation

class WebViewController: UIViewController, WKNavigationDelegate, WKUIDelegate {
    
    var webView: WKWebView!
    var command: CDVInvokedUrlCommand
    var url: URL
    var commandDelegate: CDVCommandDelegate
    var isCheckPlus: Bool = false
    
    init(command: CDVInvokedUrlCommand, delegate: CDVCommandDelegate) {
        self.command = command
        let args = command.argument(at: 0) as! NSArray
        self.url = URL(string: args[1] as! String)!
        self.commandDelegate = delegate
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        let presentUrl = webView.url?.absoluteString as? String ?? ""
        // niceid callback called here
        // http://{IP}:8080/view/checkplus_ok didStartProvisionalNavigation
        print("didFinish", presentUrl)
        
        if (presentUrl.hasSuffix("checkplus_ok")) {
            print("didFinish checkplus")
            // 플러그인 내용 작성하기
            self.webView.evaluateJavaScript("javascript:call()",  completionHandler: { [self] (result, error) in
                if (error == nil) {
                    if let result = result {
                        print("evaluateJavaScript result", result)
                        if let parsedResult = convertToJSON(resultData: convertToResultData(from: result)!) {
                            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: parsedResult)
                            self.commandDelegate.send(pluginResult, callbackId: self.command.callbackId)
                        } else {
                            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                            self.commandDelegate.send(pluginResult, callbackId: self.command.callbackId)
                        }
                    }else {
                        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                        self.commandDelegate.send(pluginResult, callbackId: self.command.callbackId)
                    }
                } else {
                    print("evaluateJavaScript error \(error!.localizedDescription)")
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                    self.commandDelegate.send(pluginResult, callbackId: self.command.callbackId)
                }
                self.dismiss(animated: false)
              })
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        let preferences = WKPreferences()
        preferences.javaScriptEnabled = true
        preferences.javaScriptCanOpenWindowsAutomatically = true
        let configuration = WKWebViewConfiguration()
        configuration.preferences = preferences
        webView = WKWebView(frame: .zero, configuration: configuration)
        webView.translatesAutoresizingMaskIntoConstraints = false
        webView.navigationDelegate = self
        webView.uiDelegate = self

        view.addSubview(webView)
        
        NSLayoutConstraint.activate([
            webView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            webView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            webView.topAnchor.constraint(equalTo: view.topAnchor),
            webView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])

        let request = URLRequest(url: self.url)
        webView.load(request)
    }
    
    func convertToResultData(from data: Any) -> ResultData? {
        guard let dictionary = data as? [String: Any] else { return nil }
        let resultData = ResultData()
        resultData.CI = dictionary["CI"] as? String
        resultData.DI = dictionary["DI"] as? String
        resultData.sAuthType = dictionary["sAuthType"] as? String
        resultData.sBirthDate = dictionary["sBirthDate"] as? String
        resultData.sGender = dictionary["sGender"] as? String
        resultData.sMobileNo = dictionary["sMobileNo"] as? String
        resultData.sName = dictionary["sName"] as? String
        resultData.sNationalInfo = dictionary["sNationalInfo"] as? String
        
        return resultData
    }
    
    func convertToJSON(resultData: ResultData) -> String? {
        let jsonEncoder = JSONEncoder()
        do {
            let jsonData = try jsonEncoder.encode(resultData)
            let jsonString = String(data: jsonData, encoding: .utf8)
            return jsonString
        } catch {
            print("Error converting to JSON: \(error)")
            return nil
        }
    }

    
}

class ResultData: Codable {
    var CI: String?
    var DI: String?
    var sAuthType: String?
    var sBirthDate: String?
    var sGender: String?
    var sMobileNo: String?
    var sName: String?
    var sNationalInfo: String?
}
