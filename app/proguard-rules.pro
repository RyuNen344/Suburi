-printconfiguration proguard-rules-merged.pro

# strip Loccat
## https://source.android.com/docs/core/tests/debug/understanding-logging#log-standards
-assumenosideeffects class android.util.Log {
  static *** v(...);
  static *** d(...);
  static *** i(...);
  static *** w(...);
  static *** e(...);
  static *** wtf(...);
  static *** isLoggable(...);
}

# androidx.compose.runtime:runtime-tracing
## https://developer.android.com/develop/ui/compose/tooling/tracing#apk_size_overhead
-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
   boolean isTraceInProgress();
   void traceEventStart(int,int,int,java.lang.String);
   void traceEventEnd();
}

# J2ObjC
## for guava
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn com.google.j2objc.annotations.RetainedWith

# OkHttp
## until okhttp 5.0
## https://github.com/square/okhttp/issues/6258
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# kotlinx-serialization
# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.
-keepattributes InnerClasses # Needed for `getDeclaredClasses`.

-if @kotlinx.serialization.Serializable class
com.example.myapplication.HasNamedCompanion, # <-- List serializable classes with named companions.
com.example.myapplication.HasNamedCompanion2
{
    static **$* *;
}
-keepnames class <1>$$serializer { # -keepnames suffices; class is kept when serializer() is kept.
    static <1>$$serializer INSTANCE;
}

# Keep both serializer and serializable classes to save the attribute InnerClasses
-keepclasseswithmembers, allowshrinking, allowobfuscation, allowaccessmodification class
com.example.myapplication.HasNamedCompanion, # <-- List serializable classes with named companions.
com.example.myapplication.HasNamedCompanion2
{
    *;
}
