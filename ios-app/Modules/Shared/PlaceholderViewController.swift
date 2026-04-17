import UIKit

class PlaceholderViewController: UIViewController {
    private let summaryText: String

    init(title: String, summaryText: String) {
        self.summaryText = summaryText
        super.init(nibName: nil, bundle: nil)
        self.title = title
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = AppTheme.Colors.background
        configureLayout()
    }

    private func configureLayout() {
        let label = UILabel()
        label.translatesAutoresizingMaskIntoConstraints = false
        label.numberOfLines = 0
        label.textAlignment = .center
        label.font = .systemFont(ofSize: 17, weight: .medium)
        label.textColor = .secondaryLabel
        label.text = summaryText

        let card = UIView()
        card.translatesAutoresizingMaskIntoConstraints = false
        card.backgroundColor = AppTheme.Colors.card
        card.layer.cornerRadius = 18
        card.layer.borderWidth = 1
        card.layer.borderColor = AppTheme.Colors.border.cgColor

        view.addSubview(card)
        card.addSubview(label)

        NSLayoutConstraint.activate([
            card.leadingAnchor.constraint(equalTo: view.layoutMarginsGuide.leadingAnchor),
            card.trailingAnchor.constraint(equalTo: view.layoutMarginsGuide.trailingAnchor),
            card.centerYAnchor.constraint(equalTo: view.centerYAnchor),

            label.topAnchor.constraint(equalTo: card.topAnchor, constant: 24),
            label.leadingAnchor.constraint(equalTo: card.leadingAnchor, constant: 20),
            label.trailingAnchor.constraint(equalTo: card.trailingAnchor, constant: -20),
            label.bottomAnchor.constraint(equalTo: card.bottomAnchor, constant: -24)
        ])
    }
}
