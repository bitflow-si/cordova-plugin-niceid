/**
  *
 */
@objc class NiceId : CDVPlugin {
    
    @objc(requestNiceId:)
    func requestNiceId(command: CDVInvokedUrlCommand) {

        let url: String? = command.arguments[0] as? String
        let param: String? = command.arguments[1] as? String
        
        let webViewController = WebViewController(url: url, param: param)
        self.viewController?.present(webViewController, animated: true, completion: nil)
        
        // 플러그인 내용 작성하기
//        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
//        print("NiceId::requestNiceId called")
//        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
}
