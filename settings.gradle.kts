pluginManagement {
    repositories {
        // 阿里云镜像源 (放在最前面)
        maven { url = java.net.URI("https://maven.aliyun.com/repository/google") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/public") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/gradle-plugin") }

        // 原生源 (作为备用)
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像源 (放在最前面)
        maven { url = java.net.URI("https://maven.aliyun.com/repository/google") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/public") }

        // 原生源 (作为备用)
        google()
        mavenCentral()
    }
}

rootProject.name = "MyLibrary"
include(":app")