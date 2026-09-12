import os, json

print("Creating Android Jetpack Compose Application Structure...")

# 1. Update settings.gradle.kts
settings_content = """pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\\\.android.*")
        includeGroupByRegex("com\\\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "Gullak Society"

include(":app")
"""

with open("settings.gradle.kts", "w", encoding="utf-8") as f:
    f.write(settings_content)

# 2. Update root build.gradle.kts
root_build_gradle = """// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
}
"""

with open("build.gradle.kts", "w", encoding="utf-8") as f:
    f.write(root_build_gradle)

# 3. Create app/build.gradle.kts
os.makedirs("app", exist_ok=True)
app_build_gradle = """plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.gullaksociety.proapp"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"

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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.okhttp)
  implementation(libs.kotlinx.coroutines.android)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
"""

with open("app/build.gradle.kts", "w", encoding="utf-8") as f:
    f.write(app_build_gradle)

# 4. AndroidManifest.xml
os.makedirs("app/src/main", exist_ok=True)
manifest_content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.GullakSociety">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.GullakSociety"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
"""

with open("app/src/main/AndroidManifest.xml", "w", encoding="utf-8") as f:
    f.write(manifest_content)

# 5. Resources
os.makedirs("app/src/main/res/values", exist_ok=True)
os.makedirs("app/src/main/res/drawable", exist_ok=True)
os.makedirs("app/src/main/res/mipmap-anydpi-v26", exist_ok=True)

strings_content = """<resources>
    <string name="app_name">Gullak Society</string>
</resources>
"""
with open("app/src/main/res/values/strings.xml", "w", encoding="utf-8") as f:
    f.write(strings_content)

colors_content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#10B981</color>
    <color name="primary_dark">#064E3B</color>
    <color name="accent">#FBBF24</color>
    <color name="bg_dark">#060913</color>
    <color name="card_bg">#0F172A</color>
    <color name="text_primary">#F8FAFC</color>
    <color name="text_secondary">#94A3B8</color>
</resources>
"""
with open("app/src/main/res/values/colors.xml", "w", encoding="utf-8") as f:
    f.write(colors_content)

themes_content = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.GullakSociety" parent="android:Theme.Material.NoActionBar">
        <item name="android:statusBarColor">#060913</item>
        <item name="android:navigationBarColor">#060913</item>
    </style>
</resources>
"""
with open("app/src/main/res/values/themes.xml", "w", encoding="utf-8") as f:
    f.write(themes_content)

# Adaptive launcher icon drawables
ic_background = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#064E3B"
        android:pathData="M0,0h108v108h-108z" />
</vector>
"""
with open("app/src/main/res/drawable/ic_launcher_background.xml", "w", encoding="utf-8") as f:
    f.write(ic_background)

ic_foreground = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <group
        android:scaleX="0.6"
        android:scaleY="0.6"
        android:translateX="21.6"
        android:translateY="21.6">
        <!-- Golden coin circle -->
        <path
            android:fillColor="#FBBF24"
            android:pathData="M54,54m-42,0a42,42 0,1,1 84,0a42,42 0,1,1 -84,0" />
        <!-- Rupee Symbol -->
        <path
            android:fillColor="#064E3B"
            android:pathData="M40,32h28v6h-28z M40,42h28v6h-28z M46,48c4,0 12,-2 12,-10c0,-8 -8,-8 -12,-8h-6v38h6v-14l16,14h10l-16,-15c6,-2 12,-7 12,-15c0,-12 -10,-15 -22,-15h-22v45h6z" />
    </group>
</vector>
"""
with open("app/src/main/res/drawable/ic_launcher_foreground.xml", "w", encoding="utf-8") as f:
    f.write(ic_foreground)

ic_launcher_xml = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""
with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml", "w", encoding="utf-8") as f:
    f.write(ic_launcher_xml)

with open("app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml", "w", encoding="utf-8") as f:
    f.write(ic_launcher_xml)

print("Base Android resources and build files created.")
