import Foundation

enum APIError: Error {
    case invalidURL
    case invalidResponse
}

struct APIClient {
    let baseURL: URL
    let session: URLSession

    init(baseURL: URL = URL(string: "https://example.com")!, session: URLSession = .shared) {
        self.baseURL = baseURL
        self.session = session
    }

    func request<T: Decodable>(_ path: String, as type: T.Type) async throws -> T {
        guard let url = URL(string: path, relativeTo: baseURL) else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        if let token = SessionStore.shared.token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, 200 ..< 300 ~= httpResponse.statusCode else {
            throw APIError.invalidResponse
        }
        return try JSONDecoder().decode(type, from: data)
    }
}
