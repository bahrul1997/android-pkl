plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "id.sch.smkn1kraksaan.pkl"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.sch.smkn1kraksaan.pkl"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ★ BASE URL ★
        buildConfigField("String", "BASE_URL", "\"https://pkl.smkn1kraksan.sch.id/\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // ★ SATU BLOK BUILD FEATURES AJA ★
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    // Bawaan Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp (logging)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Glide (load gambar)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.fragment:fragment-ktx:1.8.3")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // SwipeRefresh & CardView
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // ExifInterface
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")
}