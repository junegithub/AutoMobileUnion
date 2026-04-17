import UIKit

enum AppRouter {
    static func makeRootInterface() -> UIViewController {
        MainTabBarController()
    }

    static func makeLoginViewController() -> UIViewController {
        UINavigationController(rootViewController: LoginViewController())
    }
}
