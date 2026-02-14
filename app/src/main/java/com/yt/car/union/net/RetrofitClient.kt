package com.yt.car.union.net

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit单例客户端：全局唯一，统一配置网络请求
 */
object RetrofitClient {
    private lateinit var apiService: ApiService
    private lateinit var carApiService: CarApiService
    private lateinit var trainingApiService: TrainingApiService

    /**
     * 初始化Retrofit（建议在Application中调用）
     */
    fun init() {
        // 1. 构建OkHttp客户端
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS) // 连接超时
            .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)    // 读取超时
            .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)   // 写入超时
            .addInterceptor(TokenInterceptor()) // 添加Token拦截器
            .addInterceptor(getLoggingInterceptor())   // 添加日志拦截器（调试用）
            .build()

        // 2. 构建Retrofit
        var retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL) // 根路径
            .client(okHttpClient)     // 绑定OkHttp
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create())) // Gson解析
            .build()

        // 3. 创建ApiService实例
        apiService = retrofit.create(ApiService::class.java)
        carApiService = retrofit.create(CarApiService::class.java)

        retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL_TRAINING) // 根路径
            .client(okHttpClient)     // 绑定OkHttp
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create())) // Gson解析
            .build()
        trainingApiService = retrofit.create(TrainingApiService::class.java)
    }

    /**
     * 获取ApiService实例（全局调用）
     */
    fun getApiService(): ApiService {
        if (!::apiService.isInitialized) {
            throw UninitializedPropertyAccessException("RetrofitClient未初始化，请在Application中调用init()")
        }
        return apiService
    }

    fun getCarApiService(): CarApiService {
        if (!::carApiService.isInitialized) {
            throw UninitializedPropertyAccessException("RetrofitClient未初始化，请在Application中调用init()")
        }
        return carApiService
    }

    fun getTrainingApiService(): TrainingApiService {
        if (!::trainingApiService.isInitialized) {
            throw UninitializedPropertyAccessException("RetrofitClient未初始化，请在Application中调用init()")
        }
        return trainingApiService
    }

    /**
     * 日志拦截器：调试时打印请求/响应日志，发布时关闭
     */
    private fun getLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY // 打印所有日志（请求头、体、响应头、体）
        return interceptor
    }
}