# =========================================================================
# NexCore — ProGuard / R8 rules (production release)
# =========================================================================
#
# Compose runtime + tooling keeps no names in the APK; R8's job is to
# shrink + obfuscate everything we don't explicitly keep. Below is the
# minimal-but-correct rule set for a single-Activity Compose app that
# uses Kotlin reflection in the data layer and the public Android SDK.
# -------------------------------------------------------------------------

# -- General hygiene -----------------------------------------------------
-verbose
-allowaccessmodification
-repackageclasses

# Preserve line numbers so a crash in the wild is still actionable.
# SourceFile/OriginalSourceFile stay visible to the package map.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signatures so Kotlin reflection works at runtime.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# -- Kotlin --------------------------------------------------------------
# Kotlin metadata is required for data-class equality, sealed-class when
# over enums, and reflection on companion objects. R8's default rule set
# in proguard-android-optimize already covers most of this, but we add
# explicit safety nets for KSerializer / suspending functions.
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn org.jetbrains.annotations.**

# Keep Kotlin coroutines internals (AtomicReferenceFieldUpdater reflective
# access in ContinuationImpl).
-keepclassmembers class kotlin.coroutines.jvm.internal.** { *; }
-dontwarn kotlinx.coroutines.**

# -- AndroidX / Lifecycle / Compose -------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.tooling.preview.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.compose.**

# Compose functions are referenced reflectively for tooling + preview.
# Without this, R8 will fold them and Crashlytics-style stack traces
# become unreadable.
-keepclassmembers,allowobfuscation class * {
    @androidx.compose.runtime.Composable <methods>;
}

# -- Our sealed classes ----------------------------------------------------
# ViewModelProvider.Factory reflection looks up ViewModel constructors by
# class name; obfuscating the constructor signature works, but the class
# name itself must remain stable for viewModel(factory = ...) to resolve.
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModelProvider$Factory { *; }

# Our sealed-class Screen hierarchy is identified by name in ScreenSaver.
-keepnames class com.matox.nexcore.presentation.Screen
-keepnames class com.matox.nexcore.presentation.Screen$*
-keep class com.matox.nexcore.presentation.Screen { *; }
-keep class com.matox.nexcore.presentation.Screen$* { *; }

# -- AndroidX KTX ---------------------------------------------------------
-dontwarn androidx.core.**

# -- Network / Connectivity -----------------------------------------------
# Suppress R8 warnings on desugared / opt-in SDK references for things
# we use defensively behind Build.VERSION checks.
-dontwarn java.net.**
-dontwarn javax.net.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# -- App icon / runtime feature lookup ----------------------------------
# PackageManager reflects on ApplicationInfo / PackageItemInfo — R8's
# default rules already cover these, but explicit warning suppressions
# keep the build log clean.
-dontwarn android.content.pm.**

# -- Misc safety nets ----------------------------------------------------
# Keep our own data-class toString / componentN methods so logging
# stays meaningful even in release builds.
-keepclassmembers class com.matox.nexcore.domain.model.** {
    public *** toString();
    public *** component*();
    public *** copy(...);
}

# Compose tooling artifacts (debug-only) are stripped automatically by
# the Android Gradle Plugin's debugImplementation configuration, so we
# don't need an explicit -dontwarn for them here.