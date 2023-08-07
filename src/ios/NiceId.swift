import Foundation
import UIKit
import BackgroundTasks


@objc class NiceId : CDVPlugin {
    
    // var asyncCallbackId: String?

    @objc(requestNiceId:)
    func requestNiceId(command: CDVInvokedUrlCommand) {
        // Save the id
        // asyncCallbackId = command.callbackId
        let args = command.argument(at: 0) as! NSArray
        print("requestNiceId called", args[0], args[1])
        
        // Send no result for synchronous callback
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_NO_RESULT)
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        
        // 화면 전환 애니메이션 설정
        let webViewController = WebViewController(command: command, delegate: self.commandDelegate)
        webViewController.modalTransitionStyle = .coverVertical
        webViewController.modalPresentationStyle = .fullScreen
        let rootViewController = self.viewController!
        rootViewController.present(webViewController, animated: true, completion: nil)

    }

    // Some async callback
    /*
    @objc func didReceiveSomeAsyncResult(_ someResult: String) {
        if let callbackId = asyncCallbackId {
            // Send async result
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: someResult)
            self.commandDelegate.send(pluginResult, callbackId: callbackId)
            asyncCallbackId = nil
        }
    }
    */
}
