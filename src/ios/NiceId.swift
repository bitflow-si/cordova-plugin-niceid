@objc class NiceId : CDVPlugin {
    
    @objc(requestNiceId:)
    func requestNiceId(command: CDVInvokedUrlCommand) {

        let webViewController = self.storyboard?.instantiateViewController(withIdentifier: "webViewController") as? WebViewController else { return }
        // 화면 전환 애니메이션 설정
        webViewController.modalTransitionStyle = .coverVertical
        // 전환된 화면이 보여지는 방법 설정 (fullScreen)
        webViewController.modalPresentationStyle = .fullScreen
        self.present(webViewController, animated: true, completion: nil)
        
        // 플러그인 내용 작성하기
//        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
//        print("NiceId::requestNiceId called")
//        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
}
