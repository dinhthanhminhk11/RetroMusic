import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import java.text.SimpleDateFormat
import java.util.Calendar


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.cacheFixPlugin)
    alias(libs.plugins.firebase.crashlytics)
//    alias(libs.plugins.gms.googleServices)
    alias(libs.plugins.android.dagger.hilt)
    alias(libs.plugins.wire)
    alias(libs.plugins.ksp)
    kotlin("kapt")
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.androidx.navigation.safe.args)
}

fun getDate(): String {
    val format = "HH\'h\'-dd"
    val current = Calendar.getInstance().time
    return SimpleDateFormat(format).format(current)
}

android {
    namespace = "code.name.monkey.retromusic"
    compileSdk = 35

    defaultConfig {
        applicationId = "code.name.monkey.retromusic"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
        archivesName.set("Retro Music-($versionCode-$versionName)${getDate()}")

        ndk {
            abiFilters += listOf(
                "x86", "x86_64", "armeabi", "armeabi-v7a",
                "arm64-v8a"
            )
        }
    }

    buildTypes {
        debug {

        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    kapt {
        correctErrorTypes = true
    }

    flavorDimensions += "environment"
    productFlavors {
        create("private") {
            dimension = "environment"
            manifestPlaceholders["appLabel"] = "Retro Music Private"
            buildConfigField("String", "BASE_URL", "\"https://reqres.in/api/\"")
            buildConfigField(
                "String",
                "KEY_128",
                "\"JGjmWcjUTHDG1o+Z+oUCf6KzzKm/0TKaWc/hEVm+IIy0a22PPPwS38/F/lryy3Cz\""
            )
            buildConfigField(
                "String",
                "IV_128",
                "\"AGsyNGA8JCrxVhwjSahHv6fAkcfe3RnM/24JuJz6ogK0a22PPPwS38/F/lryy3Cz\""
            )
        }
        create("product") {
            dimension = "environment"
            manifestPlaceholders["appLabel"] = "Retro Music"
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://e0a6-113-160-45-182.ngrok-free.app/api/v1/\""
            )
            buildConfigField(
                "String",
                "BASE_URL_IMAGE_CATEGORY",
                "\"https://mms.img.susercontent.com/\""
            )
            buildConfigField(
                "String",
                "KEY_128",
                "\"JGjmWcjUTHDG1o+Z+oUCf6KzzKm/0TKaWc/hEVm+IIy0a22PPPwS38/F/lryy3Cz\""
            )
            buildConfigField(
                "String",
                "IV_128",
                "\"AGsyNGA8JCrxVhwjSahHv6fAkcfe3RnM/24JuJz6ogK0a22PPPwS38/F/lryy3Cz\""
            )
        }
    }

    configurations.configureEach {
        resolutionStrategy.force("com.google.code.findbugs:jsr305:1.3.9")
    }

    sourceSets {
        getByName("main") {
            jni {
                srcDirs("src\\main\\jniLibs")
            }
        }
    }


}

wire {
    sourcePath {
        srcDir("src/main/proto")
    }
    kotlin {
        out = "build/generated/source/wire"
    }
}


dependencies {
    implementation(project(":appthemehelper"))
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.runtime)


    implementation(libs.wire.runtime)
    implementation(libs.wire.moshi.adapter)

    // testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso)

    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.legacy.support.v4)
    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.dynamic.links)
    // hilt
    implementation(libs.dagger.hilt.library)
    //TODO hilt not yet support KSP, after support, changed to ksp
    kapt(libs.dagger.hilt.compiler)

    // api
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.json)
    implementation(libs.retrofit.converter.scalars)
    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)

    implementation(libs.glide)
    kapt(libs.glide.ksp)
    implementation(libs.glide.okhttp3.integration)
    implementation(libs.eventbus)
    //ui library
    implementation(libs.shimmer)
    implementation(libs.circleindicator)
    implementation(libs.dotsindicator)
//    implementation(libs.viewPagerIndicator)
    implementation(libs.shortcutBadger)
//    implementation(libs.android.simple.tooltip)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    // socket
    implementation("io.socket:socket.io-client:1.0.0") {
        exclude("org.json", "json")
    }
    implementation(libs.timber)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.release)

    implementation(libs.lottie)

    implementation(libs.androidx.preference.ktx)
    implementation(libs.customactivityoncrash)

    implementation(libs.afollestad.material.dialogs.core)
    implementation(libs.afollestad.material.dialogs.input)
    implementation(libs.afollestad.material.dialogs.color)
    implementation(libs.afollestad.material.cab)
    implementation(libs.kotlinx.coroutines.android)
    // change info file music
    implementation(libs.jaudiotagger)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.chrisbanes.insetter)

    //room db
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.material.intro)
    implementation(libs.tankery.circularSeekBar)
    implementation(libs.fadingedgelayout)
    implementation(libs.jetradarmobile.android.snowfall)
    implementation(libs.fastscroll.library)
    implementation(libs.androidx.mediarouter)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.advrecyclerview)
    implementation(libs.slidableactivity)
    implementation(libs.dhaval2404.imagepicker)
    implementation(libs.keyboardvisibilityevent)


}
