# Add project specific ProGuard rules here.

# --- kotlinx.serialization (R8 混淆开启后必须保留序列化器) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.salesquest.sales_quest.**$$serializer { *; }
-keepclassmembers class com.salesquest.sales_quest.** {
    *** Companion;
}
-keepclasseswithmembers class com.salesquest.sales_quest.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Room (实体/DAO 由生成代码反射访问) ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }

# --- errorprone 注解缺失（androidx.security-crypto 依赖的 tink 编译期引用，非运行必需）---
-dontwarn com.google.errorprone.annotations.**
