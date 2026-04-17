import Foundation
import WebKit

final class WebViewBridge: NSObject, WKScriptMessageHandler {
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        // Placeholder for the future H5/native bridge.
    }
}
