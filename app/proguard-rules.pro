# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep app models
-keep class com.appguardx.data.model.** { *; }
-keep class com.appguardx.analyzer.** { *; }

# Coil
-keep class coil.** { *; }
