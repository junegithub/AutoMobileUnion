import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.fx.zfcar"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        manifestPlaceholders += mapOf()
        applicationId = "com.fx.zfcar"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["WX_APPID"] = "wx2e369872d915263d"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(project.property("MYAPP_RELEASE_STORE_FILE").toString())
            storePassword = project.property("MYAPP_RELEASE_STORE_PASSWORD").toString()
            keyAlias = project.property("MYAPP_RELEASE_KEY_ALIAS").toString()
            keyPassword = project.property("MYAPP_RELEASE_KEY_PASSWORD").toString()
        }
    }


    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }

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
    implementation(libs.androidx.cardview)

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

    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.3.3")

    // ZXing核心依赖（必须）
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    // AndroidX Exif（处理图片旋转）
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    implementation("com.github.kongzue.DialogX:DialogX:0.0.51.beta1")
    implementation("com.github.kongzue.DialogXSample:DatePicker:0.0.14")

    // 微信SDK（通过jcenter，或下载官方aar包）
    implementation("com.tencent.mm.opensdk:wechat-sdk-android:6.8.0")

    implementation("com.github.loper7:DateTimePicker:0.6.3") {
        // 排除冲突的旧版依赖
        exclude (group="org.jetbrains.kotlin", module="kotlin-android-extensions-runtime")
    }
    implementation("androidx.webkit:webkit:1.10.0")

    // Media3 ExoPlayer（替代废弃的 ExoPlayer 2.x）
    implementation("androidx.media3:media3-exoplayer:1.4.0")
    implementation("androidx.media3:media3-ui:1.4.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.0") // 如需播放 HLS 流

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}