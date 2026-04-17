import Foundation

final class SessionStore {
    static let shared = SessionStore()

    private let tokenKey = "com.fx.zfcar.ios.session.token"
    private let defaults = UserDefaults.standard

    private init() {}

    var token: String? {
        get { defaults.string(forKey: tokenKey) }
        set { defaults.set(newValue, forKey: tokenKey) }
    }

    var isLoggedIn: Bool {
        guard let token, token.isEmpty == false else { return false }
        return true
    }

    func clear() {
        defaults.removeObject(forKey: tokenKey)
    }
}
