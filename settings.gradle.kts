pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // 必须添加这行，MPAndroidChart 托管在 jitpack.io
        maven ( url="https://jitpack.io" )
        maven ( url="https://maven.aliyun.com/repository/public/" )
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 必须添加这行，MPAndroidChart 托管在 jitpack.io
        maven ( url="https://jitpack.io" )
        maven ( url="https://maven.aliyun.com/repository/public/" )
    }
}

rootProject.name = "AutoMobileUnion"
include(":app")
 