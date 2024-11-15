plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.xie.autobtcodec"
    compileSdk = 32

    defaultConfig {
        applicationId = "com.xie.autobtcodec"
        minSdk = 29
        targetSdk = 32
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
        android.applicationVariants.all{

        }
        android.applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    val outputFileName = "AutoBTCodec-${variant.baseName}-${variant.versionName}.apk"
                    println("OutputFileName: $outputFileName")
                    output.outputFileName = outputFileName
                }
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lintOptions{
        isAbortOnError = false
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kapt {
        useBuildCache = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //动态权限申请框架
    implementation(libs.permissionsdispatcher)
    kapt(libs.permissionsdispatcher.processor)

}