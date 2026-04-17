import UIKit

final class MainTabBarController: UITabBarController {
    override func viewDidLoad() {
        super.viewDidLoad()
        tabBar.tintColor = AppTheme.Colors.primary
        tabBar.backgroundColor = .systemBackground
        viewControllers = [
            makeNavigationController(root: HomeViewController(), title: "首页", imageName: "house"),
            makeNavigationController(root: ReportViewController(), title: "报表", imageName: "chart.bar"),
            makeNavigationController(root: CarViewController(), title: "车辆", imageName: "car"),
            makeNavigationController(root: TrainingViewController(), title: "培训", imageName: "graduationcap"),
            makeNavigationController(root: ProfileViewController(), title: "我的", imageName: "person")
        ]
    }

    private func makeNavigationController(root: UIViewController, title: String, imageName: String) -> UINavigationController {
        root.title = title
        let navigationController = UINavigationController(rootViewController: root)
        navigationController.tabBarItem = UITabBarItem(title: title, image: UIImage(systemName: imageName), selectedImage: UIImage(systemName: "\(imageName).fill"))
        navigationController.navigationBar.prefersLargeTitles = true
        return navigationController
    }
}
