# Learnify Production ProGuard / R8 Rules

# Preserve line numbers and annotations for production crash reporting
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# Keep Gson serialization engine and data DTOs
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all Learnify DTOs, entity classes, and JSON structures
-keep class com.million.learnify.StudentAccountPreferences** { *; }
-keep class com.million.learnify.OfflineStorageManager$SubjectPackInfo { *; }
-keep class com.million.learnify.AdminConfigManager$AdminSystemConfig { *; }
-keep class com.million.learnify.ChatMessage { *; }
-keep class com.million.learnify.AIChatActivity$** { *; }
-keep class com.million.learnify.DataRepository$** { *; }

# Chaquopy Python Runtime Reflection Rules
-keep class com.chaquo.python.** { *; }
-dontwarn com.chaquo.python.**

# Retrofit 2 & OkHttp 3 Network Serialization Rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# AndroidX Security Crypto & Android Keystore
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# JavaScript Interface for Virtual Laboratory WebViews
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
