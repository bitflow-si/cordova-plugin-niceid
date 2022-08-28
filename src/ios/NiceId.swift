/**
  *
 */
@objc class NiceId : CDVPlugin {
    
    var command: CDVInvokedUrlCommand? = nil
   
    override func pluginInitialize() {
    }
    
    @objc(requestNiceId:)
    func requestNiceId(command: CDVInvokedUrlCommand) {

        self.command = command
        let url: String? = command.arguments[0] as? String
        let param: String? = command.arguments[1] as? String
        let cordovaPlugin: NiceId = self

        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_NO_RESULT)
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate!.send(pluginResult, callbackId: self.command?.callbackId)
        
        let webViewController = WebViewController(cordovaPlugin: cordovaPlugin, url: url, param: param)
        self.viewController?.present(webViewController, animated: true, completion: nil)
             
    }
    
    func getResult(data: String) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: data)
        self.commandDelegate!.send(pluginResult, callbackId: self.command?.callbackId)
    }
    
}
