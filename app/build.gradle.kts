plugins {
    id("com.android.application")
}

android {
    namespace = "com.myvideoeditor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myvideoeditor"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }
}

dependencies {
    implementation("androidx.recyclerview:recyclerview:1.4.0")
}
