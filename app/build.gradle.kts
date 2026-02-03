import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.yt.car.union"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.yt.car.union"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // 关键：使用新的compilerOptions DSL（替代旧的kotlinOptions）
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8) // 推荐：使用枚举类型（无字符串/数值争议）
            // 备选写法（字符串格式）：jvmTarget.set("1.8")
        }
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    // ViewPager2（底部Tab联动）
    implementation(libs.androidx.viewpager2)

    // Retrofit + OkHttp（网络请求）
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    // 协程（用于异步请求，可选）
    implementation(libs.kotlinx.coroutines.android)

    // 导航组件（标准化页面跳转）
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
// 地图（高德地图示例）
    implementation(libs.xdmap.v920)
    implementation(libs.location.v560)
// 图表（MPAndroidChart）
    implementation(libs.mpandroidchart)
    // 下拉刷新
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.eventbus)

    // Glide（加载图片）
    implementation(libs.glide)
    kapt(libs.compiler.v4160)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}