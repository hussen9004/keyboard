# لوحة المفاتيح العربية — تطبيق Android

مشروع Android أصلي بلغة Kotlin يوفّر لوحة مفاتيح تعمل كـ Input Method (IME)، مع تخطيط عربي وإنجليزي، زر تبديل اللغة، المسافة، الحذف، والإدخال.

## فتح وبناء المشروع

1. افتح Android Studio، واختر **Open** ثم مجلد المشروع الذي يحتوي `settings.gradle.kts`.
2. ثبّت Android SDK 35 وJDK 17 عند طلب Android Studio ذلك، ثم نفّذ **Sync Project with Gradle Files**.
3. شغّل التطبيق على جهاز Android أو محاكي. المشروع يتطلب Android Gradle Plugin 8.7.3 وGradle 8.9.
4. من التطبيق اضغط **تفعيل لوحة المفاتيح** وفعّل «لوحة المفاتيح العربية» في إعدادات النظام.
5. ارجع للتطبيق واضغط **اختيار لوحة المفاتيح**، أو اخترها من محدد لوحة المفاتيح في النظام.

يمكن إنشاء APK من Android Studio عبر **Build > Build Bundle(s) / APK(s) > Build APK(s)**. ملف APK الناتج يظهر في `app/build/outputs/apk/`.

## ملفات المشروع

- `app/src/main/java/com/hussen9004/keyboard/MainActivity.kt`: شاشة الإعداد.
- `app/src/main/java/com/hussen9004/keyboard/KeyboardService.kt`: خدمة لوحة المفاتيح وتخطيط المفاتيح.
- `app/src/main/AndroidManifest.xml`: تعريف النشاط وخدمة IME.
- `app/src/main/res/xml/method.xml`: تعريف اللغات المدعومة.

## ملاحظة الخصوصية

هذه النسخة لا تطلب صلاحية الإنترنت ولا تتضمن تحليلات أو إرسالًا للنص المكتوب. أدخل كلمات مرور أو بيانات حساسة فقط في لوحات مفاتيح تثق بها؛ لا يمكن ضمان سلوك التطبيقات الأخرى على الجهاز.
