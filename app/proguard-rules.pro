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
