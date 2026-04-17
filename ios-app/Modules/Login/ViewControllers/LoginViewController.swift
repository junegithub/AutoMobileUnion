import UIKit

final class LoginViewController: UIViewController {
    private let phoneField = UITextField()
    private let passwordField = UITextField()
    private let loginButton = UIButton(type: .system)

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "登录"
        view.backgroundColor = AppTheme.Colors.background
        configureSubviews()
    }

    private func configureSubviews() {
        let stack = UIStackView()
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .vertical
        stack.spacing = 16

        [phoneField, passwordField].forEach {
            $0.borderStyle = .roundedRect
            $0.backgroundColor = .systemBackground
            $0.heightAnchor.constraint(equalToConstant: 48).isActive = true
        }

        phoneField.placeholder = "手机号"
        phoneField.keyboardType = .phonePad

        passwordField.placeholder = "密码"
        passwordField.isSecureTextEntry = true

        loginButton.configuration = .filled()
        loginButton.configuration?.title = "模拟登录"
        loginButton.configuration?.baseBackgroundColor = AppTheme.Colors.primary
        loginButton.addTarget(self, action: #selector(handleLogin), for: .touchUpInside)

        let helperLabel = UILabel()
        helperLabel.numberOfLines = 0
        helperLabel.textAlignment = .center
        helperLabel.textColor = .secondaryLabel
        helperLabel.font = .systemFont(ofSize: 14)
        helperLabel.text = "当前为工程空壳，按钮仅写入本地 SessionStore。"

        stack.addArrangedSubview(phoneField)
        stack.addArrangedSubview(passwordField)
        stack.addArrangedSubview(loginButton)
        stack.addArrangedSubview(helperLabel)
        view.addSubview(stack)

        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.layoutMarginsGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo: view.layoutMarginsGuide.trailingAnchor),
            stack.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }

    @objc
    private func handleLogin() {
        SessionStore.shared.token = "debug-token"
        dismiss(animated: true)
    }
}
