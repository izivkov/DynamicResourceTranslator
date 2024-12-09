# DynamicResourceApi

DynamicResourceApi is an Android library that simplifies internationalization for your app. You only need to create 
a single `strings.xml` file in your native language (not necessarily English), and the library will 
automatically translate your app into the system language set on the user's phone.

### Traditional Android Project
```
res/
├── values/
│   └── strings.xml
├── values-es/
│   └── strings.xml
├── values-fr/
│   └── strings.xml
```

### Using DynamicResourceApi
```
res/
├── values/
│   └── strings.xml
```
> **Note:** Language-specific directories (e.g., `values-es/`, `values-fr/`) are optional. If they exist, the API will respect them.

## Features

- Dynamically translates default string resources at runtime.
- Eliminates the need for multiple language-specific `strings.xml` files, while respecting existing ones.
- Works with all languages.
- Preserves and used existing language specific resources.
- Supports fine-tuned translations when automatic translations are insufficient.
- Allows multiple language translations on the same page.
- Provides a pluggable translation engine architecture.
- Lightweight and easy to integrate.


## How It Works

The library intercepts calls to `getString()` and `stringResource()`, which read from `strings.xml` resource files. 
It then uses a Google translation service to translate the strings based on the language set in the phone's settings. 
Translated values are stored in local storage for reuse and better performance.

## Prerequisites
Your app must have Internet access at least the first time it runs to perform the initial translations.
Ensure you add the following permissions to your manifest:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Usage

You can access the API as follows:

1. **Initialize** `DynamicResourceApi` once, typically in `MainActivity` or your application class:
   ```kotlin
   DynamicResourceApi.init()
   ```
   Then retrieve the API anywhere in your program:
   ```kotlin
   val api = DynamicResourceApi.getApi()
   ```

2. **Replace** `context.getString` calls with `api.getString`:
   ```kotlin
   // Before:
   val text = context.getString(R.string.hello_world) 
   
   // After:
   val text = api.getString(context, R.string.hello_world)
   ```

3. **For Jetpack Compose**, replace `stringResource` with `api.stringResource`:
   ```kotlin
   // Before:
   val text = stringResource(id = R.string.hello_world)

   // After:
   val text = api.stringResource(context = LocalContext.current, id = R.string.hello_world)
   ```
## Documentation
API documentation can ge found [here](https://izivkov.github.io/DynamicResourceTranslateApi/api/org.avmedia.translateapi/-dynamic-translator/index.html):

## Fine-Tuning Translations
Override translations in one of the following tow ways:

1. **Language-Specific `strings.xml` File**  
   Add a partial `strings.xml` file for specific languages with only the strings you want to override.

   Example for Spanish (`values-es/strings.xml`):
   ```xml
   <resources>
       <string name="title_time">Hora</string>
   </resources>
   ```
   The string with the ID `R.string.title_time` will always be translated as `Hora`, regardless of the automatic translation.

2. **Providing Overrides in Code**  
   Use the `overWrites` parameter during initialization:
   ```kotlin
   DynamicResourceApi.init(
       overWrites = arrayOf(
           ResourceLocaleKey(R.string.hello, Locale("es")) to "Hola",
           ResourceLocaleKey(R.string.hello, Locale("bg")) to "Здравей %1$s",
           ResourceLocaleKey(R.string.auto_configure_settings, Locale("pt")) to "Auto",
           /* ... */
       )
   )
   ```

## Adding a Custom Translation Engine
By default, the library uses the built-in `BushTranslationEngine`, based on [this library](https://github.com/therealbush/translator) library.
You can provide your own translation engine for customized translations.
For the purposes of illustration, here’s a trivial engine that converts all strings to uppercase.

```kotlin
class UppercaseTranslationEngine : ITranslationEngine {
   override fun isInline(): Boolean = true
   override fun translate(text: String, target: Locale): String = text.uppercase()
   override suspend fun translateAsync(text: String, target: Locale): String = text.uppercase()
}
```
To use your custom engine, register it during initialization:

```kotlin
DynamicResourceApi.init(engine = UppercaseTranslationEngine())
```
After registration, all translations will use the `UppercaseTranslationEngine`.

## Credits
- This project is using the great [translator](https://github.com/therealbush/translator) Kotlin library.
- Google Translate

## Note
This is using an **unofficial** Google API. This may cease to work at any point in time, and you should be prepared to use a different translation engine if needed.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
