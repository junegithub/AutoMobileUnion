import Foundation

struct CommonResponse<T: Decodable>: Decodable {
    let code: Int
    let message: String
    let data: T?
}
