-printconfiguration proguard-rules-merged.pro

# androidx.compose.runtime:runtime-tracing
## https://developer.android.com/develop/ui/compose/tooling/tracing#apk_size_overhead
-assumenosideeffects public class androidx.compose.runtime.ComposerKt {
   boolean isTraceInProgress();
   void traceEventStart(int, java.lang.String);
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
